package br.snctech.correcao.repositorio.postgres;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormatoAssociacaoTest {

    @Test
    void leOFormatoGravadoPeloAluno() {
        Map<String, String> pares = FormatoAssociacao.ler("Java → Compilada; Python → Interpretada");
        assertEquals(Map.of("Java", "Compilada", "Python", "Interpretada"), pares);
    }

    @Test
    void aceitaSetaAscii_espacosEItensVazios() {
        Map<String, String> pares = FormatoAssociacao.ler("  A->B ;; C  →  D ; ");
        assertEquals(Map.of("A", "B", "C", "D"), pares);
    }

    @Test
    void nuloOuVazioDaMapaVazio() {
        assertTrue(FormatoAssociacao.ler(null).isEmpty());
        assertTrue(FormatoAssociacao.ler("   ").isEmpty());
    }

    @Test
    void formatarELerSaoInversos() {
        Map<String, String> original = new LinkedHashMap<>();
        original.put("1", "um");
        original.put("2", "dois");
        assertEquals("1 → um; 2 → dois", FormatoAssociacao.formatar(original));
        assertEquals(original, FormatoAssociacao.ler(FormatoAssociacao.formatar(original)));
    }
}
