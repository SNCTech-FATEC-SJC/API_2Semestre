package br.snctech.correcao.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public record NotaDoAluno(int idAvaliacao, String titulo, LocalDateTime prazo, Situacao situacao, BigDecimal nota) {

    public enum Situacao { NOTA, EM_CORRECAO, EM_ANDAMENTO, NAO_REALIZADA }

    public static NotaDoAluno de(int idAvaliacao, String titulo, LocalDateTime prazo, String statusTentativa,
                                 BigDecimal notaTotal, boolean corrigidaCompletamente, boolean correcaoFinalizada) {
        if (statusTentativa == null) {
            return new NotaDoAluno(idAvaliacao, titulo, prazo, Situacao.NAO_REALIZADA, BigDecimal.ZERO);
        }
        if ("em_andamento".equals(statusTentativa)) {
            return new NotaDoAluno(idAvaliacao, titulo, prazo, Situacao.EM_ANDAMENTO, null);
        }
        if (corrigidaCompletamente && correcaoFinalizada) {
            BigDecimal nota = notaTotal == null ? BigDecimal.ZERO : notaTotal;
            return new NotaDoAluno(idAvaliacao, titulo, prazo, Situacao.NOTA, nota);
        }
        return new NotaDoAluno(idAvaliacao, titulo, prazo, Situacao.EM_CORRECAO, null);
    }

    public String textoDaNota() {
        return switch (situacao) {
            case NOTA, NAO_REALIZADA -> nota.setScale(2, RoundingMode.HALF_UP).toPlainString()
                    .replace('.', ',') + (situacao == Situacao.NAO_REALIZADA ? "  (não realizada)" : "");
            case EM_CORRECAO -> "Aguardando Correção do Professor";
            case EM_ANDAMENTO -> "Em andamento";
        };
    }
}
