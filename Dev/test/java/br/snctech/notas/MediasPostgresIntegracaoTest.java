package br.snctech.notas;

import br.snctech.banco.Conexao;
import br.snctech.notas.model.MediaFinal;
import br.snctech.notas.model.MediaLiberada;
import br.snctech.notas.model.SituacaoFinal;
import br.snctech.notas.repositorio.RepositorioMediasPostgres;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediasPostgresIntegracaoTest {

    @BeforeAll
    static void precisaDoBancoComAMigracao03() {
        boolean ok;
        try {
            ok = !Conexao.modoMemoria() && Conexao.executar(RepositorioMediasPostgres::tabelaExiste)
                    && Conexao.executar(MediasPostgresIntegracaoTest::idDaTurmaDeExemplo) != null;
        } catch (RuntimeException e) {
            ok = false;
        }
        Assumptions.assumeTrue(ok, "Banco indisponível, sem a migração 03 ou sem o seed de exemplo: teste ignorado.");
    }

    @Test
    void leTurmaAlunosEAvaliacoesDoExemplo() {
        Conexao.executar(c -> {
            int turma = idDaTurmaDeExemplo(c);
            assertEquals(3, RepositorioMediasPostgres.alunosDaTurma(c, turma).size());
            assertFalse(RepositorioMediasPostgres.avaliacoesDaTurma(c, turma).isEmpty());
            assertEquals(turma, RepositorioMediasPostgres.turmaDaAvaliacao(c,
                    RepositorioMediasPostgres.avaliacoesDaTurma(c, turma).get(0).id()));
            return null;
        });
    }

    @Test
    void gravaLiberaELeAMediaSemAlterarOBanco() throws SQLException {
        Connection c = Conexao.obter();
        boolean autoCommit = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            int turma = idDaTurmaDeExemplo(c);
            int ana = idDoAluno(c, "ana@aluno.test.com");
            int bruno = idDoAluno(c, "bruno@aluno.test.com");
            try (PreparedStatement limpar = c.prepareStatement("DELETE FROM medias_finais WHERE turma_id = ?")) {
                limpar.setInt(1, turma);
                limpar.executeUpdate();
            }

            RepositorioMediasPostgres.sincronizar(c, turma, List.of(
                    new MediaFinal(ana, "Ana Maria", new BigDecimal("9.56"), SituacaoFinal.APROVADO, List.of()),
                    new MediaFinal(bruno, "Bruno Lima", new BigDecimal("4.83"), SituacaoFinal.EXAME_FINAL, List.of())));
            Map<Integer, Boolean> liberacao = RepositorioMediasPostgres.liberacaoPorAluno(c, turma);
            assertEquals(2, liberacao.size());
            assertFalse(liberacao.get(ana));
            assertTrue(RepositorioMediasPostgres.mediasLiberadasDoAluno(c, "ana@aluno.test.com").isEmpty());

            RepositorioMediasPostgres.liberar(c, turma);
            List<MediaLiberada> daAna = RepositorioMediasPostgres.mediasLiberadasDoAluno(c, "ANA@aluno.test.com");
            assertEquals(1, daAna.size());
            assertEquals("9,56", daAna.get(0).textoDaMedia());
            assertEquals(SituacaoFinal.APROVADO, daAna.get(0).situacao());

            RepositorioMediasPostgres.sincronizar(c, turma, List.of(
                    new MediaFinal(ana, "Ana Maria", new BigDecimal("7.00"), SituacaoFinal.APROVADO, List.of())));
            liberacao = RepositorioMediasPostgres.liberacaoPorAluno(c, turma);
            assertEquals(1, liberacao.size());
            assertTrue(liberacao.get(ana));
            assertEquals("7,00", RepositorioMediasPostgres.mediasLiberadasDoAluno(c, "ana@aluno.test.com")
                    .get(0).textoDaMedia());
        } finally {
            c.rollback();
            c.setAutoCommit(autoCommit);
        }
    }

    private static Integer idDaTurmaDeExemplo(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id FROM turmas WHERE nome = 'Turma Correção (exemplo)' ORDER BY id LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : null;
        }
    }

    private static int idDoAluno(Connection c, String email) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM usuarios WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
