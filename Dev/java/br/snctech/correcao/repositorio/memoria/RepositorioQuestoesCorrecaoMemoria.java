package br.snctech.correcao.repositorio.memoria;

import br.snctech.correcao.model.QuestaoCorrecao;
import br.snctech.correcao.repositorio.FonteDeEnunciados;
import br.snctech.correcao.repositorio.RepositorioQuestoesCorrecao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Questões de demonstração da avaliação 100 (5 questões de 2,0 pontos = 10,0). */
public class RepositorioQuestoesCorrecaoMemoria implements RepositorioQuestoesCorrecao, FonteDeEnunciados {

    public static final int ID_AVALIACAO_DEMO = 100;

    private final List<QuestaoCorrecao> questoes = List.of(
            QuestaoCorrecao.multiplaEscolha(1, new BigDecimal("2.0"), "B"),
            QuestaoCorrecao.verdadeiroFalso(2, new BigDecimal("2.0"), true),
            QuestaoCorrecao.associacao(3, new BigDecimal("2.0"), Map.of("a1", "b1", "a2", "b2")),
            QuestaoCorrecao.discursiva(4, new BigDecimal("2.0"), "Banco de dados relacional utiliza tabelas"),
            QuestaoCorrecao.discursiva(5, new BigDecimal("2.0"), "Java é uma linguagem orientada a objetos"));

    private final Map<Integer, String> enunciados = Map.of(
            1, "Qual é a capital da França? (A) Lyon  (B) Paris  (C) Marselha  (D) Nice",
            2, "Verdadeiro ou falso: MySQL é um banco de dados relacional.",
            3, "Ligue cada conceito à sua descrição. (a1→b1, a2→b2)",
            4, "O que é um banco de dados relacional?",
            5, "Por que Java é considerada uma linguagem orientada a objetos?");

    @Override
    public List<QuestaoCorrecao> buscarPorAvaliacao(Integer idAvaliacao) {
        return questoes;
    }

    @Override
    public String enunciado(int idQuestao) {
        return enunciados.getOrDefault(idQuestao, "(enunciado não encontrado)");
    }
}
