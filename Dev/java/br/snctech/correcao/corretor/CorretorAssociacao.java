package br.snctech.correcao.corretor;

import br.snctech.correcao.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Associação. DECISÃO EM ABERTO COM O GRUPO/PROFESSOR:
 * nota parcial por par correto (padrão) ou tudo-ou-nada.
 */
public class CorretorAssociacao implements CorretorQuestao {

    private final boolean parcial;

    public CorretorAssociacao() {
        this(true);
    }

    public CorretorAssociacao(boolean parcial) {
        this.parcial = parcial;
    }

    @Override
    public boolean suporta(TipoQuestao tipo) {
        return tipo == TipoQuestao.ASSOCIACAO;
    }

    @Override
    public ResultadoCorrecao corrigir(QuestaoCorrecao questao, RespostaQuestao resposta) {
        Map<String, String> gabarito = questao.gabaritoPares();
        if (gabarito.isEmpty()) {
            return new ResultadoCorrecao(BigDecimal.ZERO, StatusCorrecao.CORRIGIDA_AUTOMATICAMENTE);
        }

        long acertos = gabarito.entrySet().stream()
                .filter(e -> e.getValue().equals(resposta.getPares().get(e.getKey())))
                .count();

        BigDecimal nota;
        if (parcial) {
            nota = questao.valor()
                    .multiply(BigDecimal.valueOf(acertos))
                    .divide(BigDecimal.valueOf(gabarito.size()), 2, RoundingMode.HALF_UP);
        } else {
            nota = acertos == gabarito.size() ? questao.valor() : BigDecimal.ZERO;
        }
        return new ResultadoCorrecao(nota, StatusCorrecao.CORRIGIDA_AUTOMATICAMENTE);
    }
}
