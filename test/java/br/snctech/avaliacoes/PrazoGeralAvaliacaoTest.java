package br.snctech.avaliacoes;

import br.snctech.avaliacoes.model.Avaliacao;
import br.snctech.avaliacoes.repositorio.RepositorioAvaliacoesMemoria;
import br.snctech.avaliacoes.repositorio.RepositorioTentativasMemoria;
import br.snctech.avaliacoes.service.TentativaService;
import br.snctech.questoes.model.Questao;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PrazoGeralAvaliacaoTest {

    @Test
    void prazoGeralIgnoraDuracaoIndividualLegada() {
        RepositorioAvaliacoesMemoria avaliacoes = new RepositorioAvaliacoesMemoria();
        Avaliacao avaliacao = avaliacoes.salvar("Prova", List.of(
                        new Questao("Questão", "Dissertativa", null, null, null, null)),
                "Turma", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 20),
                LocalTime.of(8, 0), LocalTime.of(18, 0), 30);
        avaliacao.setDisponibilizada(true);

        Clock relogio = Clock.fixed(
                LocalDateTime.of(2026, 11, 20, 10, 0).atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
        TentativaService servico = new TentativaService(
                new RepositorioTentativasMemoria(), avaliacoes, relogio);

        assertNull(servico.motivoDeBloqueio(avaliacao.getId(), "aluno"));
        assertEquals(LocalDateTime.of(2026, 11, 20, 18, 0),
                servico.iniciar(avaliacao.getId(), "aluno").getLimite());
    }

    @Test
    void excluirRemoveAvaliacaoDoRepositorio() {
        RepositorioAvaliacoesMemoria avaliacoes = new RepositorioAvaliacoesMemoria();
        Avaliacao avaliacao = avaliacoes.salvar("Prova", List.of(
                        new Questao("Questão", "Dissertativa", null, null, null, null)),
                "Turma", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 20));

        avaliacoes.excluir(avaliacao.getId());

        assertNull(avaliacoes.buscarPorId(avaliacao.getId()));
        assertEquals(0, avaliacoes.listarTodas().size());
    }
}
