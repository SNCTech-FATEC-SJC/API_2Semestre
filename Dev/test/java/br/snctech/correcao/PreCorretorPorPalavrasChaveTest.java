package br.snctech.correcao;

import br.snctech.correcao.corretor.PreCorretorPorPalavrasChave;
import br.snctech.correcao.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PreCorretorPorPalavrasChaveTest {

    private final PreCorretorPorPalavrasChave pre = new PreCorretorPorPalavrasChave();
    private final QuestaoCorrecao questao = QuestaoCorrecao.discursiva(1, new BigDecimal("2.0"),
            "A fotossíntese converte luz em energia química nos cloroplastos");

    private RespostaQuestao resposta(String texto) {
        RespostaQuestao r = new RespostaQuestao(1);
        r.setTexto(texto);
        return r;
    }

    private void assertNota(String esperado, BigDecimal real) {
        assertNotNull(real);
        assertEquals(0, new BigDecimal(esperado).compareTo(real), "veio " + real);
    }

    @Test
    void respostaCompletaRecebeValorIntegral() {
        assertNota("2.0", pre.sugerirNota(questao,
                resposta("FOTOSSINTESE: converte luz em energia quimica nos CLOROPLASTOS")));
    }

    @Test
    void ignoraAcentoEMaiusculas() {
        assertNota("2.0", pre.sugerirNota(questao,
                resposta("fotossintese converte luz energia quimica cloroplastos")));
    }

    @Test
    void respostaParcialRecebeNotaProporcional() {
        // palavras relevantes do esperado: fotossintese, converte, energia, quimica, cloroplastos (5)
        // o aluno citou 2 delas
        assertNota("0.80", pre.sugerirNota(questao, resposta("Ocorre a fotossintese e gera energia")));
    }

    @Test
    void respostaEmBrancoRecebeZero() {
        assertNota("0", pre.sugerirNota(questao, resposta(null)));
    }

    @Test
    void semRespostaEsperadaNaoSugere() {
        assertNull(pre.sugerirNota(QuestaoCorrecao.discursiva(2, new BigDecimal("2.0")), resposta("qualquer texto")));
    }
}
