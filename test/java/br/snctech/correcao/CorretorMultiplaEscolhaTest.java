package br.snctech.correcao;

import br.snctech.correcao.corretor.CorretorMultiplaEscolha;
import br.snctech.correcao.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CorretorMultiplaEscolhaTest {

    private final CorretorMultiplaEscolha corretor = new CorretorMultiplaEscolha();
    private final QuestaoCorrecao questao = QuestaoCorrecao.multiplaEscolha(1, new BigDecimal("2.0"), "alt_45");

    private RespostaQuestao resposta(String alternativaId) {
        RespostaQuestao r = new RespostaQuestao(1);
        r.setAlternativaId(alternativaId);
        return r;
    }

    @Test
    void acertoRecebeValorIntegral() {
        ResultadoCorrecao res = corretor.corrigir(questao, resposta("alt_45"));
        assertEquals(0, new BigDecimal("2.0").compareTo(res.nota()));
        assertEquals(StatusCorrecao.CORRIGIDA_AUTOMATICAMENTE, res.status());
    }

    @Test
    void erroRecebeZero() {
        ResultadoCorrecao res = corretor.corrigir(questao, resposta("alt_99"));
        assertEquals(0, BigDecimal.ZERO.compareTo(res.nota()));
    }

    @Test
    void emBrancoRecebeZero() {
        ResultadoCorrecao res = corretor.corrigir(questao, resposta(null));
        assertEquals(0, BigDecimal.ZERO.compareTo(res.nota()));
    }

    @Test
    void suportaApenasMultiplaEscolha() {
        assertTrue(corretor.suporta(TipoQuestao.MULTIPLA_ESCOLHA));
        assertFalse(corretor.suporta(TipoQuestao.DISCURSIVA));
    }
}
