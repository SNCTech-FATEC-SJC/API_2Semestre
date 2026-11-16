package br.snctech.correcao.corretor;

import br.snctech.correcao.model.QuestaoCorrecao;
import br.snctech.correcao.model.RespostaQuestao;

import java.math.BigDecimal;

/**
 * Sugere uma nota para a discursiva. Está atrás de uma interface para
 * trocar o método (palavras-chave, IA, etc.) sem mexer no resto.
 */
public interface PreCorretorDiscursiva {
    /** @return nota sugerida entre 0 e o valor da questao, ou null se não há base para sugerir. */
    BigDecimal sugerirNota(QuestaoCorrecao questao, RespostaQuestao resposta);
}
