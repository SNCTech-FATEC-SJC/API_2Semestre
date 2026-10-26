package br.snctech.correcao;

import br.snctech.correcao.model.*;
import br.snctech.correcao.service.CorrecaoManualService;
import br.snctech.correcao.service.PendenciaDiscursiva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CorrecaoManualServiceTest {

    private Fakes.RespondidaEmMemoria respondidas;
    private CorrecaoManualService service;

    @BeforeEach
    void preparar() {
        respondidas = new Fakes.RespondidaEmMemoria();
        List<QuestaoCorrecao> questoes = List.of(
                QuestaoCorrecao.multiplaEscolha(1, new BigDecimal("5.0"), "alt_a"),
                QuestaoCorrecao.discursiva(2, new BigDecimal("5.0"), "texto esperado do professor"));
        service = new CorrecaoManualService(respondidas, new Fakes.QuestaoEmMemoria(questoes));

        AvaliacaoRespondida ar = new AvaliacaoRespondida(10, 100, 7);
        RespostaQuestao q1 = new RespostaQuestao(1);
        q1.setNota(new BigDecimal("5.0"));
        q1.setStatus(StatusCorrecao.CORRIGIDA_AUTOMATICAMENTE);
        RespostaQuestao q2 = new RespostaQuestao(2);
        q2.setTexto("Resposta discursiva");
        q2.setStatus(StatusCorrecao.PENDENTE_MANUAL);
        q2.setNotaSugerida(new BigDecimal("2.5"));
        ar.adicionar(q1);
        ar.adicionar(q2);
        respondidas.salvar(ar);
    }

    @Test
    void listaApenasDiscursivasPendentes() {
        List<PendenciaDiscursiva> pendentes = service.listarPendentes(100);

        assertEquals(1, pendentes.size());
        assertEquals(2, pendentes.get(0).idQuestao());
        assertEquals(0, new BigDecimal("5.0").compareTo(pendentes.get(0).valorMaximo()));
    }

    @Test
    void lancarNotaFechaAProvaQuandoNaoHaMaisPendencias() {
        AvaliacaoRespondida ar = service.lancarNota(10, 2, new BigDecimal("3.5"), 99);

        assertTrue(ar.notaFinalConfirmada());
        assertEquals(0, new BigDecimal("8.5").compareTo(ar.notaTotal()));
        assertEquals(StatusCorrecao.CORRIGIDA_MANUALMENTE, ar.resposta(2).get().getStatus());
        assertEquals(99, ar.resposta(2).get().getCorrigidoPor());
        assertTrue(service.listarPendentes(100).isEmpty());
    }

    @Test
    void pendenciaTrazRespostaEsperadaENotaSugerida() {
        PendenciaDiscursiva p = service.listarPendentes(100).get(0);
        assertEquals("texto esperado do professor", p.respostaEsperada());
        assertEquals(0, new BigDecimal("2.5").compareTo(p.notaSugerida()));
    }

    @Test
    void validarSugestaoGravaANotaSugerida() {
        AvaliacaoRespondida ar = service.validarSugestao(10, 2, 99);

        assertEquals(StatusCorrecao.CORRIGIDA_MANUALMENTE, ar.resposta(2).get().getStatus());
        assertEquals(0, new BigDecimal("2.5").compareTo(ar.resposta(2).get().getNota()));
        assertTrue(ar.notaFinalConfirmada());
    }

    @Test
    void validarSugestaoSemSugestaoLancaErro() {
        respondidas.buscar(10).resposta(2).get().setNotaSugerida(null);
        assertThrows(IllegalStateException.class, () -> service.validarSugestao(10, 2, 99));
    }

    @Test
    void notaAcimaDoValorDaQuestaoEhRejeitada() {
        assertThrows(IllegalArgumentException.class,
                () -> service.lancarNota(10, 2, new BigDecimal("5.1"), 99));
    }

    @Test
    void notaNegativaEhRejeitada() {
        assertThrows(IllegalArgumentException.class,
                () -> service.lancarNota(10, 2, new BigDecimal("-1"), 99));
    }

    @Test
    void professorPodeAlterarNotaDeQuestaoObjetivaCorrigidaAutomaticamente() {
        AvaliacaoRespondida ar = service.lancarNota(10, 1, new BigDecimal("3.0"), 99);

        RespostaQuestao q1 = ar.resposta(1).get();
        assertEquals(StatusCorrecao.CORRIGIDA_MANUALMENTE, q1.getStatus());
        assertEquals(0, new BigDecimal("3.0").compareTo(q1.getNota()));
        assertEquals(99, q1.getCorrigidoPor());
    }

    @Test
    void notaManualEmQuestaoObjetivaTambemRespeitaOValorDaQuestao() {
        assertThrows(IllegalArgumentException.class,
                () -> service.lancarNota(10, 1, new BigDecimal("5.5"), 99));
    }

    @Test
    void professorPodeAlterarDeNovoUmaCorrecaoJaFeitaPorEle() {
        service.lancarNota(10, 2, new BigDecimal("1.0"), 99);
        AvaliacaoRespondida ar = service.lancarNota(10, 2, new BigDecimal("4.0"), 98);

        assertEquals(0, new BigDecimal("4.0").compareTo(ar.resposta(2).get().getNota()));
        assertEquals(98, ar.resposta(2).get().getCorrigidoPor());
    }

    @Test
    void questaoForaDaAvaliacaoEhRejeitada() {
        assertThrows(IllegalArgumentException.class,
                () -> service.lancarNota(10, 999, new BigDecimal("1"), 99));
    }

    @Test
    void provaInexistenteLancaErroClaro() {
        assertThrows(IllegalArgumentException.class,
                () -> service.lancarNota(404, 2, new BigDecimal("1"), 99));
    }
}
