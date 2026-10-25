package br.snctech.correcao;

import br.snctech.correcao.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VereditoQuestaoTest {

    private final BigDecimal valor = new BigDecimal("2.0");

    private RespostaQuestao resposta(String nota, StatusCorrecao status) {
        RespostaQuestao r = new RespostaQuestao(1);
        r.setNota(nota == null ? null : new BigDecimal(nota));
        r.setStatus(status);
        return r;
    }

    @Test
    void notaIntegralEhCorreta() {
        assertEquals(VereditoQuestao.CORRETA,
                VereditoQuestao.de(resposta("2.0", StatusCorrecao.CORRIGIDA_AUTOMATICAMENTE), valor));
    }

    @Test
    void notaZeroEhIncorreta() {
        assertEquals(VereditoQuestao.INCORRETA,
                VereditoQuestao.de(resposta("0", StatusCorrecao.CORRIGIDA_AUTOMATICAMENTE), valor));
    }

    @Test
    void notaIntermediariaEhParcial() {
        assertEquals(VereditoQuestao.PARCIAL,
                VereditoQuestao.de(resposta("1.5", StatusCorrecao.CORRIGIDA_MANUALMENTE), valor));
    }

    @Test
    void pendenteAguardaProfessor() {
        assertEquals(VereditoQuestao.AGUARDANDO_PROFESSOR,
                VereditoQuestao.de(resposta(null, StatusCorrecao.PENDENTE_MANUAL), valor));
    }
}
