package br.snctech.notas.model;

import java.math.BigDecimal;

public record MediaLiberada(String turma, BigDecimal media, SituacaoFinal situacao) {

    public String textoDaMedia() {
        return media.toPlainString().replace('.', ',');
    }
}
