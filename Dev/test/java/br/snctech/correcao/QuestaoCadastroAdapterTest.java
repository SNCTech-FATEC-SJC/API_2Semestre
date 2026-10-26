package br.snctech.correcao;

import br.snctech.correcao.model.QuestaoCorrecao;
import br.snctech.correcao.model.TipoQuestao;
import br.snctech.correcao.service.QuestaoCadastroAdapter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Garante que os textos do cadastro do grupo (QuestaoController) viram o gabarito certo. */
class QuestaoCadastroAdapterTest {

    private static final BigDecimal DOIS = new BigDecimal("2.0");

    @Test
    void rotulosDoCadastroViramTipos() {
        assertEquals(TipoQuestao.MULTIPLA_ESCOLHA, TipoQuestao.deRotuloDoCadastro("Múltipla Escolha"));
        assertEquals(TipoQuestao.VERDADEIRO_FALSO, TipoQuestao.deRotuloDoCadastro("Verdadeiro/Falso"));
        assertEquals(TipoQuestao.DISCURSIVA, TipoQuestao.deRotuloDoCadastro("Dissertativa"));
        assertEquals(TipoQuestao.ASSOCIACAO, TipoQuestao.deRotuloDoCadastro("Ligar Colunas"));
        assertEquals(TipoQuestao.MULTIPLA_ESCOLHA, TipoQuestao.deRotuloDoCadastro("  multipla escolha "));
    }

    @Test
    void rotuloDesconhecidoEhRejeitado() {
        assertThrows(IllegalArgumentException.class, () -> TipoQuestao.deRotuloDoCadastro("Outro"));
        assertThrows(IllegalArgumentException.class, () -> TipoQuestao.deRotuloDoCadastro(null));
    }

    @Test
    void multiplaEscolhaUsaALetraComoId() {
        QuestaoCorrecao q = QuestaoCadastroAdapter.adaptar(7, "Múltipla Escolha", "c", 0, DOIS);
        assertEquals(7, q.id());
        assertEquals("C", q.gabaritoAlternativaId());
        assertEquals(TipoQuestao.MULTIPLA_ESCOLHA, q.tipo());
    }

    @Test
    void verdadeiroFalsoViraVouF() {
        assertEquals("V", QuestaoCadastroAdapter.adaptar(1, "Verdadeiro/Falso", "Verdadeiro", 0, DOIS).gabaritoAlternativaId());
        assertEquals("F", QuestaoCadastroAdapter.adaptar(1, "Verdadeiro/Falso", "Falso", 0, DOIS).gabaritoAlternativaId());
    }

    @Test
    void dissertativaGuardaOTextoEsperado() {
        QuestaoCorrecao q = QuestaoCadastroAdapter.adaptar(1, "Dissertativa", "Tabelas e chaves", 0, DOIS);
        assertEquals("Tabelas e chaves", q.respostaEsperada());
    }

    @Test
    void ligarColunasGeraIdsPorPosicao() {
        QuestaoCorrecao q = QuestaoCadastroAdapter.adaptar(1, "Ligar Colunas", "x → y; z → w", 3, DOIS);
        assertEquals(Map.of("a1", "b1", "a2", "b2", "a3", "b3"), q.gabaritoPares());
    }
}
