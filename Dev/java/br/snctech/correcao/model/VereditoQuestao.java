package br.snctech.correcao.model;

import java.math.BigDecimal;

/** Rótulo mostrado ao aluno por questão (tela "Resultado da prova"). */
public enum VereditoQuestao {
    CORRETA,
    PARCIAL,
    INCORRETA,
    AGUARDANDO_PROFESSOR;

    public static VereditoQuestao de(RespostaQuestao resposta, BigDecimal valorQuestao) {
        if (resposta.getStatus() == null || resposta.getStatus() == StatusCorrecao.PENDENTE_MANUAL) {
            return AGUARDANDO_PROFESSOR;
        }
        BigDecimal nota = resposta.getNota() == null ? BigDecimal.ZERO : resposta.getNota();
        if (nota.compareTo(valorQuestao) >= 0) {
            return CORRETA;
        }
        return nota.signum() == 0 ? INCORRETA : PARCIAL;
    }
}
