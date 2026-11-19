package br.snctech.notas.model;

import java.math.BigDecimal;

public record TentativaRegistrada(int idAluno, int idAvaliacao, String status, BigDecimal notaTotal,
                                  boolean corrigidaCompletamente, boolean correcaoFinalizada) {
}
