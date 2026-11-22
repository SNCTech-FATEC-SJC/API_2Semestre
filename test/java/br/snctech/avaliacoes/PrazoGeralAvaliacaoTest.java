package br.snctech.avaliacoes;

import br.snctech.avaliacoes.model.Avaliacao;
import br.snctech.avaliacoes.model.Tentativa;
import br.snctech.avaliacoes.repositorio.RepositorioAvaliacoesMemoria;
import br.snctech.avaliacoes.repositorio.RepositorioTentativasMemoria;
import br.snctech.avaliacoes.service.TentativaService;
import br.snctech.questoes.model.Questao;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PrazoGeralAvaliacaoTest {

    private static final class RelogioControlavel extends Clock {
        private final ZoneId zona = ZoneId.systemDefault();
        private Instant agora;

        RelogioControlavel(LocalDateTime inicio) {
            definir(inicio);
        }

        void definir(LocalDateTime momento) {
            agora = momento.atZone(zona).toInstant();
        }

        @Override public ZoneId getZone() { return zona; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return agora; }
    }

    @Test
    void prazoGeralEhOLimiteQuandoNaoHaDuracaoIndividual() {
        RepositorioAvaliacoesMemoria avaliacoes = new RepositorioAvaliacoesMemoria();
        Avaliacao avaliacao = avaliacoes.salvar("Prova", List.of(
                        new Questao("Questão", "Dissertativa", null, null, null, null)),
                "Turma", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 20),
                LocalTime.of(8, 0), LocalTime.of(18, 0), 0);
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
    void timerUsaODuracaoIndividualOuOPrazoGeralOQueChegarPrimeiro() {
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

        assertEquals(LocalDateTime.of(2026, 11, 20, 10, 30),
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

    @Test
    void cronometroEntregaAutomaticamenteAoAtingirZero() {
        LocalDate dia = LocalDate.of(2026, 11, 20);
        RepositorioAvaliacoesMemoria avaliacoes = new RepositorioAvaliacoesMemoria();
        Avaliacao avaliacao = avaliacoes.salvar("Prova", List.of(
                        new Questao("Questão", "Dissertativa", null, null, null, null)),
                "Turma", dia, dia, LocalTime.of(8, 0), LocalTime.of(18, 0), 1);
        avaliacao.setDisponibilizada(true);
        RepositorioTentativasMemoria tentativas = new RepositorioTentativasMemoria();
        RelogioControlavel relogio = new RelogioControlavel(dia.atTime(8, 0));
        TentativaService servico = new TentativaService(tentativas, avaliacoes, relogio);
        Tentativa tentativa = servico.iniciar(avaliacao.getId(), "aluno");
        Questao questao = avaliacao.getQuestoes().get(0);
        servico.responder(tentativa.getId(), questao, "Resposta salva");

        relogio.definir(dia.atTime(8, 0, 59, 500_000_000));
        assertEquals(1, servico.segundosRestantes(tentativa.getId()));
        relogio.definir(dia.atTime(8, 1));

        assertEquals(0, servico.segundosRestantes(tentativa.getId()));
        assertEquals(Tentativa.Status.FINALIZADA, tentativa.getStatus());
        assertEquals(dia.atTime(8, 1), tentativa.getFim());
        assertEquals("Resposta salva", tentativa.getResposta(questao));
    }

    @Test
    void fecharTelaNoLimiteTambemEntregaAutomaticamente() {
        LocalDate dia = LocalDate.of(2026, 11, 20);
        RepositorioAvaliacoesMemoria avaliacoes = new RepositorioAvaliacoesMemoria();
        Avaliacao avaliacao = avaliacoes.salvar("Prova", List.of(
                        new Questao("Questão", "Dissertativa", null, null, null, null)),
                "Turma", dia, dia, LocalTime.of(8, 0), LocalTime.of(8, 1), 0);
        avaliacao.setDisponibilizada(true);
        RelogioControlavel relogio = new RelogioControlavel(dia.atTime(8, 0));
        TentativaService servico = new TentativaService(
                new RepositorioTentativasMemoria(), avaliacoes, relogio);
        Tentativa tentativa = servico.iniciar(avaliacao.getId(), "aluno");
        relogio.definir(dia.atTime(8, 1));

        assertEquals(Tentativa.Status.FINALIZADA, servico.encerrar(tentativa.getId()).getStatus());
    }
}
