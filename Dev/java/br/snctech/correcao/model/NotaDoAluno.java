package br.snctech.correcao.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Uma linha da tela "Minhas notas" do aluno (PB-11): uma avaliação já encerrada ou já entregue.
 *
 * <p>A nota só aparece quando a prova está totalmente corrigida (sem discursiva pendente).
 * Enquanto isso o aluno vê "Em correção", porque a nota ainda pode mudar.
 */
public record NotaDoAluno(int idAvaliacao, String titulo, LocalDateTime prazo, Situacao situacao, BigDecimal nota) {

    public enum Situacao { NOTA, EM_CORRECAO, EM_ANDAMENTO, NAO_REALIZADA }

    /**
     * Decide o que mostrar ao aluno.
     *
     * @param statusTentativa null = o aluno nunca abriu a prova; senão em_andamento | enviada | nao_realizada_prazo
     * @param notaTotal nota gravada na tentativa (pode ser null)
     * @param corrigidaCompletamente true quando não resta nenhuma questão para o professor corrigir
     */
    public static NotaDoAluno de(int idAvaliacao, String titulo, LocalDateTime prazo,
                                 String statusTentativa, BigDecimal notaTotal, boolean corrigidaCompletamente) {
        if (statusTentativa == null) {
            // Quem não abriu a prova até o encerramento fica com nota 0.
            return new NotaDoAluno(idAvaliacao, titulo, prazo, Situacao.NAO_REALIZADA, BigDecimal.ZERO);
        }
        if ("em_andamento".equals(statusTentativa)) {
            return new NotaDoAluno(idAvaliacao, titulo, prazo, Situacao.EM_ANDAMENTO, null);
        }
        if (corrigidaCompletamente) {
            BigDecimal nota = notaTotal == null ? BigDecimal.ZERO : notaTotal;
            return new NotaDoAluno(idAvaliacao, titulo, prazo, Situacao.NOTA, nota);
        }
        return new NotaDoAluno(idAvaliacao, titulo, prazo, Situacao.EM_CORRECAO, null);
    }

    /** Texto pronto para a tela. */
    public String textoDaNota() {
        return switch (situacao) {
            case NOTA, NAO_REALIZADA -> nota.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
                    .replace('.', ',') + (situacao == Situacao.NAO_REALIZADA ? "  (não realizada)" : "");
            case EM_CORRECAO -> "Em correção";
            case EM_ANDAMENTO -> "Em andamento";
        };
    }
}
