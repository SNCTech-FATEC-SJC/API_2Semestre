package br.snctech.banco;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreparadorBancoIntegracaoTest {

    private static final String NOME = "snctech_teste_preparo";
    private static String servidor;
    private static Path pasta;

    @BeforeAll
    static void precisaDoServidor() {
        pasta = PreparadorBanco.localizarPasta();
        Assumptions.assumeTrue(pasta != null && !Conexao.modoMemoria(), "Sem a pasta de scripts: teste ignorado.");
        servidor = Conexao.url().replaceFirst("^(jdbc:postgresql://[^/]+/).*$", "$1");
        try {
            apagarBancoDeTeste();
        } catch (SQLException e) {
            Assumptions.abort("PostgreSQL indisponível: teste ignorado. " + e.getMessage());
        }
    }

    @AfterAll
    static void limpar() throws SQLException {
        if (servidor != null) apagarBancoDeTeste();
    }

    @Test
    void criaOBancoDoZeroERodaCadaScriptUmaVezSo() throws SQLException {
        PreparadorBanco preparador = new PreparadorBanco(servidor + NOME, Conexao.usuario(), Conexao.senha(), pasta);

        PreparadorBanco.Resultado primeira = preparador.preparar();
        assertTrue(primeira.bancoCriado());
        assertEquals(PreparadorBanco.SCRIPT_BASE, primeira.scriptsAplicados().get(0));
        assertTrue(primeira.scriptsAplicados().contains("migracao_05_notas_e_medias.sql"));
        assertTrue(primeira.scriptsAplicados().contains("seed_05_medias_exemplo.sql"));

        PreparadorBanco.Resultado segunda = preparador.preparar();
        assertFalse(segunda.bancoCriado());
        assertTrue(segunda.scriptsAplicados().isEmpty());

        try (Connection c = DriverManager.getConnection(servidor + NOME, Conexao.usuario(), Conexao.senha());
             Statement s = c.createStatement()) {
            assertEquals(1, umNumero(s, "SELECT count(*) FROM avaliacoes WHERE titulo = 'Prova 2 - Média (exemplo)'"));
            assertEquals(1, umNumero(s, "SELECT count(*) FROM information_schema.columns "
                    + "WHERE table_name = 'tentativas_avaliacao' AND column_name = 'correcao_finalizada'"));
            assertEquals(primeira.scriptsAplicados().size(), umNumero(s, "SELECT count(*) FROM controle_scripts"));
        }
    }

    private static int umNumero(Statement s, String sql) throws SQLException {
        try (ResultSet rs = s.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static void apagarBancoDeTeste() throws SQLException {
        try (Connection c = DriverManager.getConnection(servidor + "postgres", Conexao.usuario(), Conexao.senha());
             Statement s = c.createStatement()) {
            s.execute("DROP DATABASE IF EXISTS " + NOME + " WITH (FORCE)");
        }
    }
}
