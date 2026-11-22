package br.snctech.notas.model;

public enum SituacaoFinal {
    APROVADO("Aprovado"),
    EXAME_FINAL("Exame final"),
    REPROVADO("Reprovado"),
    PENDENTE("Média ainda não disponível");

    private final String rotulo;

    SituacaoFinal(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
