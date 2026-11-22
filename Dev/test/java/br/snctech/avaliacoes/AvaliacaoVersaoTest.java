package br.snctech.avaliacoes;

import br.snctech.avaliacoes.model.Avaliacao;
import br.snctech.avaliacoes.repositorio.RepositorioAvaliacoes;
import br.snctech.avaliacoes.repositorio.RepositorioAvaliacoesMemoria;
import br.snctech.avaliacoes.service.AvaliacaoService;
import br.snctech.questoes.model.Questao;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvaliacaoVersaoTest {

    @Test
    void editarAvaliacaoRespondidaCriaNovaVersaoSemAlterarOriginal() {
        RepositorioAvaliacoes repositorio = new RepositorioAvaliacoesMemoria();
        AvaliacaoService service = new AvaliacaoService(repositorio);
        Questao original = questao("Original");
        Questao alterada = questao("Alterada");

        Avaliacao avaliacao = service.criar(
                "Prova",
                List.of(original),
                "1A",
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 20),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                60);
        avaliacao.setRespondida(true);

        Avaliacao nova = service.editar(
                avaliacao.getId(),
                "Prova",
                List.of(alterada),
                "1A",
                LocalDate.of(2026, 11, 21),
                LocalDate.of(2026, 11, 21),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                60);

        assertNotEquals(avaliacao.getId(), nova.getId());
        assertEquals("Prova2", nova.getNome());
        assertEquals(avaliacao.getId(), nova.getAvaliacaoPaiId());
        assertEquals(2, nova.getVersao());
        assertFalse(nova.isRespondida());
        assertEquals("Original", repositorio.buscarPorId(avaliacao.getId()).getQuestoes().get(0).getEnunciado());
    }

    private Questao questao(String enunciado) {
        return new Questao(enunciado, "Dissertativa", null, null, null, null);
    }
}
