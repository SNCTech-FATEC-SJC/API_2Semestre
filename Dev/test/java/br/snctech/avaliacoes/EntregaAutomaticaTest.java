package br.snctech.avaliacoes;

import br.snctech.avaliacoes.model.Avaliacao;
import br.snctech.avaliacoes.model.Tentativa;
import br.snctech.avaliacoes.model.TurmaAvaliacao;
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

class EntregaAutomaticaTest {

    @Test
    void entregaAoEsgotarOTempoMantendoAsRespostasSalvas() {
        ZoneId zona = ZoneId.systemDefault();
        RelogioFalso relogio = new RelogioFalso(LocalDateTime.of(2026, 11, 20, 9, 0), zona);
        RepositorioAvaliacoesMemoria avaliacoes = new RepositorioAvaliacoesMemoria();
        RepositorioTentativasMemoria tentativas = new RepositorioTentativasMemoria();
        TentativaService servico = new TentativaService(tentativas, avaliacoes, relogio);
        Questao questao = new Questao("Questão", "Dissertativa", null, null, null, null);
        Avaliacao avaliacao = avaliacoes.salvar(
                "Prova",
                List.of(questao),
                new TurmaAvaliacao(1, "1A"),
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 20),
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                1);
        avaliacao.setDisponibilizada(true);

        Tentativa tentativa = servico.iniciar(avaliacao.getId(), "aluno@exemplo.com");
        servico.responder(tentativa.getId(), questao, "Resposta antes do limite");

        relogio.irPara(LocalDateTime.of(2026, 11, 20, 9, 0, 59, 500_000_000));
        assertEquals(1, servico.segundosRestantes(tentativa.getId()));
        assertEquals(Tentativa.Status.EM_ANDAMENTO, tentativa.getStatus());

        relogio.irPara(LocalDateTime.of(2026, 11, 20, 9, 1));
        assertEquals(0, servico.segundosRestantes(tentativa.getId()));
        assertEquals(Tentativa.Status.FINALIZADA, tentativa.getStatus());
        assertEquals("Resposta antes do limite", tentativa.getResposta(questao));
        assertEquals(LocalDateTime.of(2026, 11, 20, 9, 1), tentativa.getFim());
    }

    private static final class RelogioFalso extends Clock {
        private final ZoneId zona;
        private Instant agora;

        private RelogioFalso(LocalDateTime momento, ZoneId zona) {
            this.zona = zona;
            irPara(momento);
        }

        private void irPara(LocalDateTime momento) {
            agora = momento.atZone(zona).toInstant();
        }

        @Override
        public ZoneId getZone() {
            return zona;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new RelogioFalso(LocalDateTime.ofInstant(agora, zone), zone);
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }
}
