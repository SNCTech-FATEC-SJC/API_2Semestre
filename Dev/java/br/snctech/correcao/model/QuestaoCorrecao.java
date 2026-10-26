package br.snctech.correcao.model;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Visão da questão para a correção (não depende de banco nem de tela).
 *
 * <p>Não confundir com {@code br.snctech.Questao}, que é o cadastro de questões do grupo
 * (enunciado, imagem, alternativas). Esta classe só guarda o que a correção precisa:
 * tipo, gabarito e quanto a questão vale. A conversão está em
 * {@link br.snctech.correcao.service.QuestaoCadastroAdapter}.
 *
 * @param id                    id da questão (mesmo id do cadastro; {@code int} como no resto do projeto)
 * @param valor                 pontos da questão dentro da avaliação (definidos pelo professor)
 * @param gabaritoAlternativaId id da alternativa correta: "A".."D" (múltipla escolha) ou "V"/"F" (verdadeiro/falso)
 * @param gabaritoPares         pares corretos "idItemA -> idItemB" (associação)
 * @param respostaEsperada      texto cadastrado pelo professor (discursiva, usado na pré-correção)
 * @param textosAlternativas    id da alternativa -> texto, para as telas mostrarem o texto em vez do id
 *                              (opcional; vazio nos dados de demonstração)
 */
public record QuestaoCorrecao(Integer id,
                              TipoQuestao tipo,
                              BigDecimal valor,
                              String gabaritoAlternativaId,
                              Map<String, String> gabaritoPares,
                              String respostaEsperada,
                              Map<String, String> textosAlternativas) {

    public QuestaoCorrecao {
        gabaritoPares = gabaritoPares == null ? Map.of() : Map.copyOf(gabaritoPares);
        textosAlternativas = textosAlternativas == null ? Map.of() : Map.copyOf(textosAlternativas);
    }

    /** Sem textos de alternativas (dados de demonstração e testes). */
    public QuestaoCorrecao(Integer id, TipoQuestao tipo, BigDecimal valor, String gabaritoAlternativaId,
                           Map<String, String> gabaritoPares, String respostaEsperada) {
        this(id, tipo, valor, gabaritoAlternativaId, gabaritoPares, respostaEsperada, null);
    }

    /** Texto de uma alternativa, ou null se esta questão não tem o texto cadastrado. */
    public String textoDaAlternativa(String idAlternativa) {
        return idAlternativa == null ? null : textosAlternativas.get(idAlternativa);
    }

    public static QuestaoCorrecao multiplaEscolha(Integer id, BigDecimal valor, String alternativaCorretaId) {
        return new QuestaoCorrecao(id, TipoQuestao.MULTIPLA_ESCOLHA, valor, alternativaCorretaId, null, null);
    }

    /** @param verdadeira true se o gabarito é "Verdadeiro" (id "V"); false se é "Falso" (id "F"). */
    public static QuestaoCorrecao verdadeiroFalso(Integer id, BigDecimal valor, boolean verdadeira) {
        return new QuestaoCorrecao(id, TipoQuestao.VERDADEIRO_FALSO, valor, verdadeira ? "V" : "F", null, null);
    }

    public static QuestaoCorrecao associacao(Integer id, BigDecimal valor, Map<String, String> pares) {
        return new QuestaoCorrecao(id, TipoQuestao.ASSOCIACAO, valor, null, pares, null);
    }

    public static QuestaoCorrecao discursiva(Integer id, BigDecimal valor, String respostaEsperada) {
        return new QuestaoCorrecao(id, TipoQuestao.DISCURSIVA, valor, null, null, respostaEsperada);
    }

    public static QuestaoCorrecao discursiva(Integer id, BigDecimal valor) {
        return discursiva(id, valor, null);
    }
}
