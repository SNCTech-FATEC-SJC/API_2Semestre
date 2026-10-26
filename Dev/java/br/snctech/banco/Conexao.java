package br.snctech.banco;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Conexão com o PostgreSQL. A aplicação usa UMA conexão compartilhada (é um app de mesa, com uma
 * única thread de interface), que é reaberta sozinha se cair.
 *
 * Onde ficam as configurações (a primeira que existir vence):
 *   1. propriedade do Java (-Dsnctech.db.url=...), 2. variável de ambiente (SNCTECH_DB_URL...),
 *   3. arquivo banco.properties (na pasta do projeto ou em ./Dev), 4. valores padrão.
 *
 * Chaves: db.url, db.usuario, db.senha e modo (banco | memoria).
 */
public final class Conexao {
    private static final String URL_PADRAO = "jdbc:postgresql://localhost:5432/SNCTech_DB";
    private static final String USUARIO_PADRAO = "postgres";

    private static Properties arquivo;
    private static Connection conexao;

    private Conexao() { }

    @FunctionalInterface
    public interface Operacao<T> {
        T executar(Connection conexao) throws SQLException;
    }

    /** modo=memoria roda sem banco (nada é salvo). Útil para testes rápidos e demonstrações. */
    public static boolean modoMemoria() {
        return "memoria".equalsIgnoreCase(config("modo", "snctech.modo", "SNCTECH_MODO", "banco").trim());
    }

    public static synchronized Connection obter() {
        try {
            if (conexao == null || conexao.isClosed() || !conexao.isValid(2)) {
                conexao = DriverManager.getConnection(
                        config("db.url", "snctech.db.url", "SNCTECH_DB_URL", URL_PADRAO),
                        config("db.usuario", "snctech.db.usuario", "SNCTECH_DB_USUARIO", USUARIO_PADRAO),
                        config("db.senha", "snctech.db.senha", "SNCTECH_DB_SENHA", ""));
            }
            return conexao;
        } catch (SQLException e) {
            throw new ErroBanco("Não foi possível conectar ao banco de dados. Confira o banco.properties "
                    + "e se o PostgreSQL está rodando. Detalhe: " + e.getMessage(), e);
        }
    }

    /** Roda uma operação simples (cada comando é confirmado na hora). */
    public static synchronized <T> T executar(Operacao<T> operacao) {
        Connection c = obter();
        try {
            return operacao.executar(c);
        } catch (SQLException e) {
            throw new ErroBanco("Erro ao acessar o banco de dados: " + e.getMessage(), e);
        }
    }

    /** Roda várias operações como uma só: ou grava tudo, ou não grava nada. */
    public static synchronized <T> T emTransacao(Operacao<T> operacao) {
        Connection c = obter();
        try {
            boolean autoCommitAnterior = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                T resultado = operacao.executar(c);
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                try {
                    c.rollback();
                } catch (SQLException ignorada) {
                    // a falha original é a que importa
                }
                throw e;
            } finally {
                c.setAutoCommit(autoCommitAnterior);
            }
        } catch (SQLException e) {
            throw new ErroBanco("Erro ao gravar no banco de dados: " + e.getMessage(), e);
        }
    }

    /** null se o banco já tem tudo o que a aplicação usa; senão, a mensagem do que falta. */
    public static String verificarEsquema() {
        try {
            executar(c -> {
                // Consultas sem retornar linhas: o PostgreSQL valida se todas as colunas usadas
                // pelos DAOs e repositórios realmente existem antes de abrir a interface.
                String[] consultas = {
                    "SELECT id, turma_id, titulo, versao, avaliacao_pai_id, liberada, liberada_em, prazo_fim, inicio, duracao_minutos FROM avaliacoes WHERE 1 = 0",
                    "SELECT id, nome, email, perfil FROM usuarios WHERE 1 = 0",
                    "SELECT id, nome, professor_id, ativa FROM turmas WHERE 1 = 0",
                    "SELECT id, professor_id, tipo, enunciado, imagem_url FROM questoes WHERE 1 = 0",
                    "SELECT id, questao_id, texto, correta, par_associacao FROM alternativas WHERE 1 = 0",
                    "SELECT avaliacao_id, questao_id, ordem FROM avaliacao_questoes WHERE 1 = 0",
                    "SELECT id, avaliacao_id, aluno_id, data_inicio, data_envio, limite, status FROM tentativas_avaliacao WHERE 1 = 0",
                    "SELECT id, tentativa_id, questao_id, alternativa_escolhida_id, resposta_discursiva, resposta_associacao FROM respostas_aluno WHERE 1 = 0"
                };
                try (Statement s = c.createStatement()) {
                    for (String consulta : consultas) {
                        try (var rs = s.executeQuery(consulta)) {
                            // Basta preparar e executar: WHERE 1 = 0 não lê registros.
                        }
                    }
                }
                return null;
            });
            return null;
        } catch (ErroBanco e) {
            return "O banco não está com o esquema esperado. Restaure o DB_SQL/snctech_backup.sql e rode o "
                    + "DB_SQL/migracao_01_integracao_avaliacoes.sql. Detalhe: " + e.getMessage();
        }
    }

    public static synchronized void fechar() {
        if (conexao != null) {
            try {
                conexao.close();
            } catch (SQLException ignorada) {
                // encerrando de qualquer jeito
            }
            conexao = null;
        }
    }

    // ---------------------------------------------------------------- configuração

    private static String config(String chaveArquivo, String propriedade, String variavel, String padrao) {
        String valor = System.getProperty(propriedade);
        if (valor != null && !valor.isBlank()) return valor;
        valor = System.getenv(variavel);
        if (valor != null && !valor.isBlank()) return valor;
        valor = lerArquivo().getProperty(chaveArquivo);
        if (valor != null && !valor.isBlank()) return valor.trim();
        return padrao;
    }

    private static synchronized Properties lerArquivo() {
        if (arquivo != null) return arquivo;
        arquivo = new Properties();
        String caminho = System.getProperty("snctech.config");
        File[] candidatos = caminho != null
                ? new File[]{new File(caminho)}
                : new File[]{new File("banco.properties"), new File("Dev/banco.properties")};
        for (File candidato : candidatos) {
            if (candidato.isFile()) {
                try (Reader leitor = new InputStreamReader(new FileInputStream(candidato), StandardCharsets.UTF_8)) {
                    arquivo.load(leitor);
                } catch (IOException e) {
                    System.err.println("Não foi possível ler " + candidato + ": " + e.getMessage());
                }
                break;
            }
        }
        return arquivo;
    }
}
