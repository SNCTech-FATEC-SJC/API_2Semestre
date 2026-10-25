package br.snctech.correcao.service;

import br.snctech.correcao.model.QuestaoCorrecao;
import br.snctech.correcao.model.TipoQuestao;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ponte entre o cadastro de questões do grupo (PB-02, classe {@code br.snctech.Questao}) e a correção.
 *
 * <p>Recebe só tipos simples (texto/número) para a correção não depender das telas do cadastro.
 * Quem integra faz algo como:
 * <pre>
 *   QuestaoCorrecao qc = QuestaoCadastroAdapter.adaptar(
 *           q.getId(), q.getTipo(), q.getRespostaCorreta(), q.getPares().size(), valorDefinidoPeloProfessor);
 * </pre>
 *
 * <p>Como cada tipo do cadastro vira gabarito da correção:
 * <ul>
 *   <li><b>Múltipla Escolha</b>: respostaCorreta "A".."D" vira o id da alternativa (as 4 alternativas têm id fixo A–D).</li>
 *   <li><b>Verdadeiro/Falso</b>: "Verdadeiro" vira "V" e "Falso" vira "F".</li>
 *   <li><b>Dissertativa</b>: respostaCorreta é o texto esperado (usado na pré-correção por palavras-chave).</li>
 *   <li><b>Ligar Colunas</b>: o par de posição i (1, 2, 3…) da lista de pares do cadastro tem ids
 *       {@code a<i>} (termoA) e {@code b<i>} (termoB); o gabarito é sempre a{i} → b{i}. O embaralhamento (PB-04)
 *       muda só a ordem de exibição, nunca esses ids.</li>
 * </ul>
 */
public final class QuestaoCadastroAdapter {

    private QuestaoCadastroAdapter() {
    }

    public static QuestaoCorrecao adaptar(int id, String tipoDoCadastro, String respostaCorreta,
                                          int quantidadeDePares, BigDecimal valor) {
        TipoQuestao tipo = TipoQuestao.deRotuloDoCadastro(tipoDoCadastro);
        return switch (tipo) {
            case MULTIPLA_ESCOLHA -> QuestaoCorrecao.multiplaEscolha(id, valor, respostaCorreta == null ? null : respostaCorreta.trim().toUpperCase());
            case VERDADEIRO_FALSO -> QuestaoCorrecao.verdadeiroFalso(id, valor, "verdadeiro".equalsIgnoreCase(respostaCorreta == null ? "" : respostaCorreta.trim()));
            case DISCURSIVA -> QuestaoCorrecao.discursiva(id, valor, respostaCorreta);
            case ASSOCIACAO -> QuestaoCorrecao.associacao(id, valor, paresPorPosicao(quantidadeDePares));
        };
    }

    /** {"a1": "b1", "a2": "b2", ...} para N pares. */
    public static Map<String, String> paresPorPosicao(int quantidadeDePares) {
        Map<String, String> pares = new LinkedHashMap<>();
        for (int i = 1; i <= quantidadeDePares; i++) {
            pares.put(idItemA(i), idItemB(i));
        }
        return pares;
    }

    /** Id do item da coluna A do par de posição {@code posicao} (começa em 1). */
    public static String idItemA(int posicao) {
        return "a" + posicao;
    }

    /** Id do item da coluna B do par de posição {@code posicao} (começa em 1). */
    public static String idItemB(int posicao) {
        return "b" + posicao;
    }
}
