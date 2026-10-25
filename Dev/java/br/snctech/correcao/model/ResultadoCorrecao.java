package br.snctech.correcao.model;

import java.math.BigDecimal;

/**
 * Resultado de corrigir uma questão.
 * Nota é null quando a correção está pendente; notaSugerida só existe na pré-correção da discursiva.
 */
public record ResultadoCorrecao(BigDecimal nota, StatusCorrecao status, BigDecimal notaSugerida) {

    public ResultadoCorrecao(BigDecimal nota, StatusCorrecao status) {
        this(nota, status, null);
    }
}
