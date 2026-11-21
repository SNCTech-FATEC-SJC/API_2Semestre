package br.snctech.avaliacoes;

import br.snctech.avaliacoes.service.ApoioResposta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class ApoioRespostaTest {

    @Test
    void embaralhaSemAlterarListaOriginal() {
        List<String> alternativas = List.of("A", "B", "C", "D");

        List<String> exibidas = ApoioResposta.embaralharAlternativas(alternativas, 10, 25);

        assertNotSame(alternativas, exibidas);
        assertEquals(alternativas, List.of("A", "B", "C", "D"));
    }

    @Test
    void usaOrdemEstavelParaAMesmaTentativaEQuestao() {
        List<String> alternativas = List.of("A", "B", "C", "D");

        List<String> primeira = ApoioResposta.embaralharAlternativas(alternativas, 10, 25);
        List<String> segunda = ApoioResposta.embaralharAlternativas(alternativas, 10, 25);

        assertEquals(primeira, segunda);
    }
}
