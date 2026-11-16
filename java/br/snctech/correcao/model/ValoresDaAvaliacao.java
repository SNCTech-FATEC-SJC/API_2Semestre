package br.snctech.correcao.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Regras do "valor de cada questão" dentro de uma avaliação.
 *
 * <p>O professor define quanto vale cada questão (no banco: coluna a combinar, sugestão avaliacao_questoes.valor).
 * A prova vale de 0 a 10, então a soma dos valores deve ser 10. Se o professor não definir,
 * divide-se 10 igualmente.
 */
public final class ValoresDaAvaliacao {

    public static final BigDecimal NOTA_MAXIMA = BigDecimal.TEN;

    private ValoresDaAvaliacao() {
    }

    /**
     * Divide 10 igualmente. A última questão recebe o resto, para a soma dar exatamente 10
     * (ex.: 3 questões = 3,33 + 3,33 + 3,34).
     */
    public static Map<Integer, BigDecimal> dividirIgualmente(List<Integer> idsQuestoes) {
        Map<Integer, BigDecimal> valores = new LinkedHashMap<>();
        if (idsQuestoes.isEmpty()) {
            return valores;
        }
        BigDecimal base = NOTA_MAXIMA.divide(BigDecimal.valueOf(idsQuestoes.size()), 2, RoundingMode.DOWN);
        BigDecimal acumulado = BigDecimal.ZERO;
        for (int i = 0; i < idsQuestoes.size() - 1; i++) {
            valores.put(idsQuestoes.get(i), base);
            acumulado = acumulado.add(base);
        }
        valores.put(idsQuestoes.get(idsQuestoes.size() - 1), NOTA_MAXIMA.subtract(acumulado));
        return valores;
    }

    /** A soma dos valores informados pelo professor fecha exatamente 10? */
    public static boolean somaFechaDez(Collection<BigDecimal> valores) {
        BigDecimal soma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.compareTo(NOTA_MAXIMA) == 0;
    }

    /**
     * Para a tela de montar avaliação (PB-01) chamar antes de salvar.
     *
     * @throws IllegalArgumentException se algum valor for nulo/≤ 0 ou se a soma não for 10
     */
    public static void validar(Collection<BigDecimal> valores) {
        if (valores.isEmpty()) {
            throw new IllegalArgumentException("A avaliação não tem questões.");
        }
        for (BigDecimal v : valores) {
            if (v == null || v.signum() <= 0) {
                throw new IllegalArgumentException("Cada questão deve valer mais que zero.");
            }
        }
        if (!somaFechaDez(valores)) {
            BigDecimal soma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            throw new IllegalArgumentException(
                    "A soma dos valores das questões deve ser 10 (atual: " + soma.stripTrailingZeros().toPlainString() + ").");
        }
    }
}
