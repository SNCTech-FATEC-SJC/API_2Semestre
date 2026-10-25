package br.snctech.correcao.corretor;

import br.snctech.correcao.model.QuestaoCorrecao;
import br.snctech.correcao.model.ResultadoCorrecao;
import br.snctech.correcao.model.RespostaQuestao;
import br.snctech.correcao.model.TipoQuestao;

/** Strategy: uma implementação por tipo de questão. */
public interface CorretorQuestao {
    boolean suporta(TipoQuestao tipo);
    ResultadoCorrecao corrigir(QuestaoCorrecao questao, RespostaQuestao resposta);
}
