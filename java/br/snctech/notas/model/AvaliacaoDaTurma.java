package br.snctech.notas.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AvaliacaoDaTurma(int id, String titulo, BigDecimal peso, LocalDateTime prazo) {

    public boolean encerradaEm(LocalDateTime momento) {
        return prazo != null && prazo.isBefore(momento);
    }

    public String textoDoPeso() {
        return peso.stripTrailingZeros().toPlainString().replace('.', ',');
    }
}
