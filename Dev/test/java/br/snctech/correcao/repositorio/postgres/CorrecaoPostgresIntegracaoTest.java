package br.snctech.correcao.repositorio.postgres;

import br.snctech.banco.Conexao;
import br.snctech.correcao.model.AvaliacaoRespondida;
import br.snctech.correcao.model.QuestaoCorrecao;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de integração: só roda se houver banco com a migração 02 e o seed de exemplo
 * (DB_SQL/seed_02_correcao_exemplo.sql). Sem banco, é ignorado — não quebra o build de ninguém.
 * Só LÊ dados; não altera o banco.
 */
class CorrecaoPostgresIntegracaoTest {

    @BeforeAll
    static void precisaDoBanco() {
        boolean ok;
        try {
            ok = !Conexao.modoMemoria() && EsquemaCorrecao.verificar() == null;
        } catch (RuntimeException e) {
            ok = false;
        }
        Assumptions.assumeTrue(ok, "Banco indisponível ou sem a migração 02: teste ignorado.");
    }

    @Test
    void esquemaEstaCompleto() {
        assertNull(EsquemaCorrecao.verificar());
    }

    @Test
    void leQuestoesEValoresDaAvaliacaoDeExemplo() {
        Integer av = idDaProvaDeExemplo();
        Assumptions.assumeTrue(av != null, "Seed de exemplo não aplicado.");
        List<QuestaoCorrecao> questoes = new RepositorioQuestoesCorrecaoPostgres().buscarPorAvaliacao(av);
        assertEquals(4, questoes.size());
        assertEquals(0, questoes.stream().map(QuestaoCorrecao::valor)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .compareTo(new java.math.BigDecimal("10")));
    }

    @Test
    void leTentativasEntregues() {
        Integer av = idDaProvaDeExemplo();
        Assumptions.assumeTrue(av != null, "Seed de exemplo não aplicado.");
        List<AvaliacaoRespondida> entregues = new RepositorioAvaliacoesRespondidasPostgres().buscarPorAvaliacao(av);
        assertTrue(entregues.size() >= 2);
    }

    private static Integer idDaProvaDeExemplo() {
        return Conexao.executar(c -> {
            try (var ps = c.prepareStatement("SELECT id FROM avaliacoes WHERE titulo = 'Prova de Exemplo - Correção'");
                 var rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        });
    }
}
