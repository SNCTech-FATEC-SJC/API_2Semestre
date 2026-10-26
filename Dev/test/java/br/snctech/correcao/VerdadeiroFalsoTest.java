package br.snctech.correcao;

import br.snctech.correcao.corretor.CorretorMultiplaEscolha;
import br.snctech.correcao.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class VerdadeiroFalsoTest {

    private final CorretorMultiplaEscolha corretor = new CorretorMultiplaEscolha();

    private RespostaQuestao resposta(String id) {
        RespostaQuestao r = new RespostaQuestao(1);
        r.setAlternativaId(id);
        return r;
    }

    @Test
    void corretorSuportaVerdadeiroFalso() {
        assertTrue(corretor.suporta(TipoQuestao.VERDADEIRO_FALSO));
        assertTrue(corretor.suporta(TipoQuestao.MULTIPLA_ESCOLHA));
        assertFalse(corretor.suporta(TipoQuestao.ASSOCIACAO));
    }

    @Test
    void acertarVerdadeiroDaValorIntegral() {
        QuestaoCorrecao q = QuestaoCorrecao.verdadeiroFalso(1, new BigDecimal("2.0"), true);
        ResultadoCorrecao res = corretor.corrigir(q, resposta("V"));
        assertEquals(0, new BigDecimal("2.0").compareTo(res.nota()));
        assertEquals(StatusCorrecao.CORRIGIDA_AUTOMATICAMENTE, res.status());
    }

    @Test
    void acertarFalsoDaValorIntegral() {
        QuestaoCorrecao q = QuestaoCorrecao.verdadeiroFalso(1, new BigDecimal("2.0"), false);
        assertEquals(0, new BigDecimal("2.0").compareTo(corretor.corrigir(q, resposta("F")).nota()));
    }

    @Test
    void erroOuEmBrancoRecebeZero() {
        QuestaoCorrecao q = QuestaoCorrecao.verdadeiroFalso(1, new BigDecimal("2.0"), true);
        assertEquals(0, BigDecimal.ZERO.compareTo(corretor.corrigir(q, resposta("F")).nota()));
        assertEquals(0, BigDecimal.ZERO.compareTo(corretor.corrigir(q, resposta(null)).nota()));
    }
}
