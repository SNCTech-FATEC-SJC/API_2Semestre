package br.snctech.correcao;

import br.snctech.correcao.corretor.CorretorAssociacao;
import br.snctech.correcao.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CorretorAssociacaoTest {

    private final QuestaoCorrecao questao = QuestaoCorrecao.associacao(1, new BigDecimal("4.0"),
            Map.of("A", "2", "B", "1", "C", "4", "D", "3"));

    private RespostaQuestao resposta(Map<String, String> pares) {
        RespostaQuestao r = new RespostaQuestao(1);
        r.setPares(pares);
        return r;
    }

    private void assertNota(String esperado, ResultadoCorrecao res) {
        assertEquals(0, new BigDecimal(esperado).compareTo(res.nota()),
                "esperado " + esperado + " mas veio " + res.nota());
    }

    @Test
    void todosOsParesCertos() {
        assertNota("4.0", new CorretorAssociacao().corrigir(questao,
                resposta(Map.of("A", "2", "B", "1", "C", "4", "D", "3"))));
    }

    @Test
    void notaParcialPorPar() {
        assertNota("2.0", new CorretorAssociacao().corrigir(questao,
                resposta(Map.of("A", "2", "B", "1", "C", "3", "D", "4"))));
    }

    @Test
    void tudoOuNadaZeraQuandoUmParErra() {
        assertNota("0", new CorretorAssociacao(false).corrigir(questao,
                resposta(Map.of("A", "2", "B", "1", "C", "3", "D", "4"))));
    }

    @Test
    void semRespostaRecebeZero() {
        assertNota("0", new CorretorAssociacao().corrigir(questao, resposta(Map.of())));
    }
}
