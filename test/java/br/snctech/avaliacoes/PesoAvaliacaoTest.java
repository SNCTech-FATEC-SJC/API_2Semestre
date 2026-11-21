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
import static org.junit.jupiter.api.Assertions.assertThrows;

class PesoAvaliacaoTest {
    private static final LocalDate DATA = LocalDate.of(2026, 11, 20);
    private static final LocalTime INICIO = LocalTime.of(8, 0);
    private static final LocalTime FIM = LocalTime.of(10, 0);

    @Test
    void criaAvaliacaoComPesoDefinido() {
        RepositorioAvaliacoes repo = new RepositorioAvaliacoesMemoria();
        AvaliacaoService service = new AvaliacaoService(repo);

        Avaliacao avaliacao = service.criar("Prova", List.of(questao()), "1A",
                DATA, DATA, INICIO, FIM, 60, 2.5);

        assertEquals(2.5, avaliacao.getPeso(), 0.0001);
        assertEquals(2.5, repo.buscarPorId(avaliacao.getId()).getPeso(), 0.0001);
    }

    @Test
    void editaPesoSemCriarVersaoQuandoAindaNaoFoiRespondida() {
        RepositorioAvaliacoes repo = new RepositorioAvaliacoesMemoria();
        AvaliacaoService service = new AvaliacaoService(repo);
        Avaliacao avaliacao = service.criar("Prova", List.of(questao()), "1A",
                DATA, DATA, INICIO, FIM, 60, 1.0);

        Avaliacao editada = service.editar(avaliacao.getId(), "Prova", List.of(questao()),
                "1A", DATA, DATA, INICIO, FIM, 60, 3.0);

        assertEquals(avaliacao.getId(), editada.getId());
        assertEquals(3.0, repo.buscarPorId(avaliacao.getId()).getPeso(), 0.0001);
    }

    @Test
    void novaVersaoDeAvaliacaoRespondidaRecebePesoInformadoESemAlterarOriginal() {
        RepositorioAvaliacoes repo = new RepositorioAvaliacoesMemoria();
        AvaliacaoService service = new AvaliacaoService(repo);
        Avaliacao original = service.criar("Prova", List.of(questao()), "1A",
                DATA, DATA, INICIO, FIM, 60, 1.5);
        original.setRespondida(true);

        Avaliacao nova = service.editar(original.getId(), "Prova", List.of(questao()),
                "1A", DATA, DATA, INICIO, FIM, 60, 2.75);

        assertEquals(1.5, repo.buscarPorId(original.getId()).getPeso(), 0.0001);
        assertEquals(2.75, nova.getPeso(), 0.0001);
    }

    @Test
    void rejeitaPesoInvalido() {
        AvaliacaoService service = new AvaliacaoService(new RepositorioAvaliacoesMemoria());
        assertThrows(IllegalArgumentException.class, () -> service.criar("Prova",
                List.of(questao()), "1A", DATA, DATA, INICIO, FIM, 60, 0));
        assertThrows(IllegalArgumentException.class, () -> service.criar("Prova",
                List.of(questao()), "1A", DATA, DATA, INICIO, FIM, 60, 1000));
    }

    private Questao questao() {
        return new Questao("Enunciado", "Dissertativa", null, null, null, null);
    }
}
