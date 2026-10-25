package br.snctech.correcao.corretor;

import br.snctech.correcao.model.*;

/**
 * Não dá a nota final: calcula uma nota SUGERIDA e deixa a questão pendente
 * até o professor validar ou alterar.
 */
public class CorretorDiscursiva implements CorretorQuestao {

    private final PreCorretorDiscursiva preCorretor;

    public CorretorDiscursiva(PreCorretorDiscursiva preCorretor) {
        this.preCorretor = preCorretor;
    }

    @Override
    public boolean suporta(TipoQuestao tipo) {
        return tipo == TipoQuestao.DISCURSIVA;
    }

    @Override
    public ResultadoCorrecao corrigir(QuestaoCorrecao questao, RespostaQuestao resposta) {
        return new ResultadoCorrecao(null, StatusCorrecao.PENDENTE_MANUAL,
                preCorretor.sugerirNota(questao, resposta));
    }
}
