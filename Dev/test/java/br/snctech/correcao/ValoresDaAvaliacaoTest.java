package br.snctech.correcao;

import br.snctech.correcao.model.ValoresDaAvaliacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValoresDaAvaliacaoTest {

    private static List<BigDecimal> lista(String... v) {
        return java.util.Arrays.stream(v).map(BigDecimal::new).toList();
    }

    @Test
    void divisaoIgualSempreSomaDez() {
        Map<Integer, BigDecimal> v = ValoresDaAvaliacao.dividirIgualmente(List.of(1, 2, 3));
        assertEquals(new BigDecimal("3.33"), v.get(1));
        assertEquals(new BigDecimal("3.33"), v.get(2));
        assertEquals(new BigDecimal("3.34"), v.get(3));
        assertTrue(ValoresDaAvaliacao.somaFechaDez(v.values()));
    }

    @Test
    void divisaoIgualSemQuestoesDevolveVazio() {
        assertTrue(ValoresDaAvaliacao.dividirIgualmente(List.of()).isEmpty());
    }

    @Test
    void valoresDoProfessorQueSomamDezSaoAceitos() {
        assertDoesNotThrow(() -> ValoresDaAvaliacao.validar(lista("2.5", "2.5", "5")));
    }

    @Test
    void somaDiferenteDeDezEhRejeitada() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ValoresDaAvaliacao.validar(lista("2", "3")));
        assertTrue(e.getMessage().contains("10"));
    }

    @Test
    void valorZeroOuNegativoEhRejeitado() {
        assertThrows(IllegalArgumentException.class, () -> ValoresDaAvaliacao.validar(lista("0", "10")));
        assertThrows(IllegalArgumentException.class, () -> ValoresDaAvaliacao.validar(lista("-1", "11")));
    }

    @Test
    void avaliacaoSemQuestoesEhRejeitada() {
        assertThrows(IllegalArgumentException.class, () -> ValoresDaAvaliacao.validar(List.of()));
    }
}
