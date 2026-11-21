package br.snctech.banco;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class PreparadorBanco {

    public static final String SCRIPT_BASE = "snctech_backup.sql";

    private static final Pattern PARTES_DA_URL = Pattern.compile("^(jdbc:postgresql://[^/]+/)([^?;]+)(.*)$");
    private static final Pattern NOME_DO_SCRIPT = Pattern.compile("^(migracao|seed)_(\\d+)_.+\\.sql$");
    private static final Pattern BEGIN_OU_COMMIT = Pattern.compile("(?im)^\\s*(BEGIN|COMMIT)\\s*;[ \\t]*$");

    public enum Falha { SENHA, SERVIDOR, PASTA, SCRIPT }

    public static final class ProblemaNoBanco extends RuntimeException {
        private final Falha falha;

        public ProblemaNoBanco(Falha falha, String mensagem, Throwable causa) {
            super(mensagem, causa);
            this.falha = falha;
        }

        public Falha falha() {
            return falha;
        }
    }

    public record Resultado(boolean bancoCriado, List<String> scriptsAplicados) {
    }

    private final String url;
    private final String usuario;
    private final String senha;
    private final Path pasta;

    public PreparadorBanco(String url, String usuario, String senha, Path pasta) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha == null ? "" : senha;
        this.pasta = pasta;
    }

    public static PreparadorBanco padrao() {
        return new PreparadorBanco(Conexao.url(), Conexao.usuario(), Conexao.senha(), localizarPasta());
    }

    public static Path localizarPasta() {
        String escolhida = System.getProperty("snctech.scripts");
        List<Path> candidatas = escolhida != null
                ? List.of(Path.of(escolhida))
                : List.of(Path.of("Avisos.e.Banco", "DB_SQL"), Path.of("Dev", "Avisos.e.Banco", "DB_SQL"));
        for (Path candidata : candidatas) {
            if (Files.isRegularFile(candidata.resolve(SCRIPT_BASE))) return candidata;
        }
        return null;
    }

    public Resultado preparar() {
        boolean criado = false;
        Connection conexao;
        try {
            conexao = conectar(url);
        } catch (SQLException e) {
            if (!"3D000".equals(e.getSQLState())) throw traduzir(e);
            criarBanco();
            criado = true;
            try {
                conexao = conectar(url);
            } catch (SQLException e2) {
                throw traduzir(e2);
            }
        }
        try (Connection c = conexao) {
            return new Resultado(criado, aplicarScripts(c));
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    public static List<String> scriptsEmOrdem(List<String> nomes) {
        List<String> scripts = new ArrayList<>();
        for (String nome : nomes) {
            if (NOME_DO_SCRIPT.matcher(nome).matches()) scripts.add(nome);
        }
        scripts.sort(Comparator
                .comparingInt((String nome) -> numero(nome))
                .thenComparing(nome -> nome.startsWith("seed_") ? 1 : 0)
                .thenComparing(Comparator.naturalOrder()));
        return scripts;
    }

    public static String semBeginECommit(String sql) {
        String texto = sql.startsWith("﻿") ? sql.substring(1) : sql;
        return BEGIN_OU_COMMIT.matcher(texto).replaceAll("");
    }

    private static int numero(String nome) {
        Matcher m = NOME_DO_SCRIPT.matcher(nome);
        m.matches();
        return Integer.parseInt(m.group(2));
    }

    private List<String> aplicarScripts(Connection c) throws SQLException {
        List<String> aplicadosAgora = new ArrayList<>();
        boolean estruturaExiste = tabelaExiste(c, "usuarios");
        if (pasta == null) {
            if (estruturaExiste) return aplicadosAgora;
            throw new ProblemaNoBanco(Falha.PASTA, "O banco está vazio e a pasta Avisos.e.Banco/DB_SQL não foi "
                    + "encontrada. Rode o programa a partir da pasta Dev (mvn javafx:run).", null);
        }
        try (Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS public.controle_scripts ("
                    + "arquivo character varying(200) PRIMARY KEY, "
                    + "aplicado_em timestamp NOT NULL DEFAULT now())");
        }
        Set<String> jaAplicados = lerAplicados(c);
        if (!jaAplicados.contains(SCRIPT_BASE)) {
            if (estruturaExiste) {
                registrar(c, SCRIPT_BASE);
            } else {
                executar(c, SCRIPT_BASE);
                aplicadosAgora.add(SCRIPT_BASE);
            }
        }
        for (String script : scriptsEmOrdem(listarPasta())) {
            if (jaAplicados.contains(script)) continue;
            executar(c, script);
            aplicadosAgora.add(script);
        }
        return aplicadosAgora;
    }

    private List<String> listarPasta() {
        try (Stream<Path> arquivos = Files.list(pasta)) {
            return arquivos.filter(Files::isRegularFile).map(p -> p.getFileName().toString()).toList();
        } catch (IOException e) {
            throw new ProblemaNoBanco(Falha.PASTA, "Não foi possível ler a pasta " + pasta.toAbsolutePath()
                    + ": " + e.getMessage(), e);
        }
    }

    private void executar(Connection c, String arquivo) throws SQLException {
        String sql;
        try {
            sql = semBeginECommit(Files.readString(pasta.resolve(arquivo), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ProblemaNoBanco(Falha.PASTA, "Não foi possível ler o script " + arquivo + ": "
                    + e.getMessage(), e);
        }
        c.setAutoCommit(false);
        try (Statement s = c.createStatement()) {
            s.execute(sql);
            registrar(c, arquivo);
            c.commit();
        } catch (SQLException e) {
            c.rollback();
            throw new ProblemaNoBanco(Falha.SCRIPT, "Erro ao aplicar o script " + arquivo + ". Nada dele foi "
                    + "gravado. Detalhe: " + e.getMessage(), e);
        } finally {
            c.setAutoCommit(true);
            try (Statement s = c.createStatement()) {
                s.execute("RESET search_path");
            }
        }
    }

    private static void registrar(Connection c, String arquivo) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO public.controle_scripts (arquivo) VALUES (?) ON CONFLICT DO NOTHING")) {
            ps.setString(1, arquivo);
            ps.executeUpdate();
        }
    }

    private static Set<String> lerAplicados(Connection c) throws SQLException {
        Set<String> nomes = new HashSet<>();
        try (Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT arquivo FROM public.controle_scripts")) {
            while (rs.next()) nomes.add(rs.getString(1));
        }
        return nomes;
    }

    private static boolean tabelaExiste(Connection c, String tabela) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT to_regclass(?) IS NOT NULL")) {
            ps.setString(1, "public." + tabela);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        }
    }

    private void criarBanco() {
        Matcher partes = PARTES_DA_URL.matcher(url);
        if (!partes.matches()) {
            throw new ProblemaNoBanco(Falha.SERVIDOR, "O endereço do banco (db.url) não está no formato "
                    + "jdbc:postgresql://servidor:porta/nome: " + url, null);
        }
        String nome = partes.group(2);
        try (Connection c = conectar(partes.group(1) + "postgres" + partes.group(3));
             Statement s = c.createStatement()) {
            s.execute("CREATE DATABASE \"" + nome.replace("\"", "\"\"") + "\" TEMPLATE template0 ENCODING 'UTF8'");
        } catch (SQLException e) {
            if ("42P04".equals(e.getSQLState())) return;
            throw traduzir(e);
        }
    }

    private Connection conectar(String endereco) throws SQLException {
        return DriverManager.getConnection(endereco, usuario, senha);
    }

    private ProblemaNoBanco traduzir(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        String texto = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (estado.equals("28P01") || estado.equals("28000") || texto.contains("password")
                || texto.contains("senha")) {
            return new ProblemaNoBanco(Falha.SENHA, senha.isBlank()
                    ? "Falta informar a senha do PostgreSQL deste computador."
                    : "A senha do PostgreSQL está errada para o usuário \"" + usuario + "\".", e);
        }
        if (estado.startsWith("08")) {
            return new ProblemaNoBanco(Falha.SERVIDOR, "Não foi possível falar com o PostgreSQL. Confira se ele "
                    + "está instalado e ligado (no Windows: serviço postgresql-x64-18). Detalhe: "
                    + e.getMessage(), e);
        }
        return new ProblemaNoBanco(Falha.SCRIPT, "Erro ao preparar o banco: " + e.getMessage(), e);
    }
}
