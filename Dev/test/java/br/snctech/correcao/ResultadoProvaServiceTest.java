package br.snctech.correcao;

import br.snctech.correcao.model.*;
import br.snctech.correcao.service.ResultadoProvaService;
import br.snctech.correcao.service.ResultadoProvaService.ResultadoProva;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultadoProvaServiceTest {

    @Test
    void provaComDiscursivaPendenteFicaParcialESemNotaNaQuestao() {
        Fakes.RespondidaEmMemoria respondidas = new Fakes.RespondidaEmMemoria();
        List<QuestaoCorrecao> questoes = List.of(
                QuestaoCorrecao.multiplaEscolha(1, new BigDecimal("5.0"), "A"),
                QuestaoCorrecao.discursiva(2, new BigDecimal("5.0")));

        AvaliacaoRespondida ar = new AvaliacaoRespondida(10, 100, 7);
        RespostaQuestao q1 = new RespostaQuestao(1);
        q1.setNota(new BigDecimal("5.0"));
        q1.setStatus(StatusCorrecao.CORRIGIDA_AUTOMATICAMENTE);
        RespostaQuestao q2 = new RespostaQuestao(2);
        q2.setStatus(StatusCorrecao.PENDENTE_MANUAL);
        q2.setNotaSugerida(new BigDecimal("3.0"));
        ar.adicionar(q1);
        ar.adicionar(q2);
        respondidas.salvar(ar);

        ResultadoProva res = new ResultadoProvaService(respondidas, new Fakes.QuestaoEmMemoria(questoes))
                .resultadoDoAluno(10);

        assertFalse(res.notaFinal());
        assertEquals(VereditoQuestao.CORRETA, res.itens().get(0).veredito());
        assertEquals(VereditoQuestao.AGUARDANDO_PROFESSOR, res.itens().get(1).veredito());
        assertNull(res.itens().get(1).nota()); // aluno não vê nota provisória nem a sugestão
    }
}
