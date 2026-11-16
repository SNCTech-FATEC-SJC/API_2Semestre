package br.snctech.correcao.corretor;

import br.snctech.correcao.model.QuestaoCorrecao;
import br.snctech.correcao.model.RespostaQuestao;
import br.snctech.correcao.model.ResultadoCorrecao;
import br.snctech.correcao.model.StatusCorrecao;
import br.snctech.correcao.model.TipoQuestao;

import java.math.BigDecimal;

/**
 * Múltipla escolha e Verdadeiro/Falso (que é uma múltipla escolha de 2 opções: "V" e "F").
 * Compara pelo ID da alternativa, nunca pela posição (assim o embaralhamento do PB-04 não afeta).
 */
public class CorretorMultiplaEscolha implements CorretorQuestao {

    @Override
    public boolean suporta(TipoQuestao tipo) {
        return tipo == TipoQuestao.MULTIPLA_ESCOLHA || tipo == TipoQuestao.VERDADEIRO_FALSO;
    }

    @Override
    public ResultadoCorrecao corrigir(QuestaoCorrecao questao, RespostaQuestao resposta) {
        boolean acertou = resposta.getAlternativaId() != null
                && resposta.getAlternativaId().equals(questao.gabaritoAlternativaId());
        BigDecimal nota = acertou ? questao.valor() : BigDecimal.ZERO;
        return new ResultadoCorrecao(nota, StatusCorrecao.CORRIGIDA_AUTOMATICAMENTE);
    }
}
