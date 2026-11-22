package br.snctech.notas.model;

import java.math.BigDecimal;
import java.util.List;

public record MediaFinal(int idAluno, String nomeAluno, BigDecimal media, SituacaoFinal situacao,
                         List<String> pendencias) {
    public MediaFinal {
        pendencias = List.copyOf(pendencias);
    }

    public boolean estaPronta() {
        return situacao != SituacaoFinal.PENDENTE;
    }

    public String textoDaMedia() {
        return media == null ? "–" : media.toPlainString().replace('.', ',');
    }
}
