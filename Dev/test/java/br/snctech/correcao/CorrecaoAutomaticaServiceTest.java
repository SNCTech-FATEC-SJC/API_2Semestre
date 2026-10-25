package br.snctech.correcao;

import br.snctech.correcao.corretor.*;
import br.snctech.correcao.model.*;
import br.snctech.correcao.service.CorrecaoAutomaticaService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CorrecaoAutomaticaServiceTest {

    private final List<QuestaoCorrecao> questoes = List.of(
            QuestaoCorrecao.multiplaEscolha(1, new BigDecimal("3.0"), "alt_a"),
            QuestaoCorrecao.associacao(2, new BigDecimal("2.0"), Map.of("A", "1", "B", "2")),
            QuestaoCorrecao.discursiva(3, new BigDecimal("5.0"), "fotossintese converte energia"));

    private CorrecaoAutomaticaService service() {
        return new CorrecaoAutomaticaService(
                List.of(new CorretorMultiplaEscolha(), new CorretorAssociacao(), new CorretorDiscursiva(new PreCorretorPorPalavrasChave())),
                new Fakes.RespondidaEmMemoria(),
                new Fakes.QuestaoEmMemoria(questoes));
    }

    private AvaliacaoRespondida provaDoAluno() {
        AvaliacaoRespondida ar = new AvaliacaoRespondida(10, 100, 7);
        RespostaQuestao q1 = new RespostaQuestao(1);
        q1.setAlternativaId("alt_a");
        RespostaQuestao q2 = new RespostaQuestao(2);
        q2.setPares(Map.of("A", "1", "B", "9"));
        RespostaQuestao q3 = new RespostaQuestao(3);
        q3.setTexto("A fotossintese converte luz");
        ar.adicionar(q1);
        ar.adicionar(q2);
        ar.adicionar(q3);
        return ar;
    }

    @Test
    void corrigeObjetivasEDeixaDiscursivaPendente() {
        AvaliacaoRespondida ar = provaDoAluno();
        service().corrigir(ar, questoes);

        assertEquals(StatusCorrecao.CORRIGIDA_AUTOMATICAMENTE, ar.resposta(1).get().getStatus());
        assertEquals(StatusCorrecao.CORRIGIDA_AUTOMATICAMENTE, ar.resposta(2).get().getStatus());
        assertEquals(StatusCorrecao.PENDENTE_MANUAL, ar.resposta(3).get().getStatus());

        // 3.0 (múltipla escolha) + 1.0 (associação, 1 de 2 pares)
        assertEquals(0, new BigDecimal("4.0").compareTo(ar.notaTotal()));
        assertTrue(ar.temPendencias());
        assertFalse(ar.notaFinalConfirmada());
    }

    @Test
    void discursivaRecebeNotaSugeridaMasContinuaPendente() {
        AvaliacaoRespondida ar = provaDoAluno();
        service().corrigir(ar, questoes);

        RespostaQuestao disc = ar.resposta(3).get();
        assertNull(disc.getNota());
        // palavras relevantes: fotossintese, converte, energia -> aluno citou 2 de 3 => 5.0 * 2/3
        assertEquals(0, new BigDecimal("3.33").compareTo(disc.getNotaSugerida()));
    }

    @Test
    void questaoEmBrancoViraRespostaVaziaComNotaZero() {
        AvaliacaoRespondida ar = new AvaliacaoRespondida(11, 100, 8); // aluno não respondeu nada
        service().corrigir(ar, questoes);

        assertEquals(0, BigDecimal.ZERO.compareTo(ar.resposta(1).get().getNota()));
        assertEquals(3, ar.respostas().size());
    }

    @Test
    void naoSobrescreveCorrecaoManual() {
        AvaliacaoRespondida ar = provaDoAluno();
        RespostaQuestao disc = ar.resposta(3).get();
        disc.setNota(new BigDecimal("4.5"));
        disc.setStatus(StatusCorrecao.CORRIGIDA_MANUALMENTE);

        service().corrigir(ar, questoes);

        assertEquals(StatusCorrecao.CORRIGIDA_MANUALMENTE, disc.getStatus());
        assertEquals(0, new BigDecimal("4.5").compareTo(disc.getNota()));
    }

    @Test
    void tipoSemCorretorLancaErro() {
        CorrecaoAutomaticaService semCorretores = new CorrecaoAutomaticaService(
                List.of(), new Fakes.RespondidaEmMemoria(), new Fakes.QuestaoEmMemoria(questoes));
        assertThrows(IllegalStateException.class,
                () -> semCorretores.corrigir(provaDoAluno(), questoes));
    }
}
