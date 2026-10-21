package br.snctech;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class BancoQuestoes {

    private static final ObservableList<Questao> QUESTOES =
            FXCollections.observableArrayList();

    private static Questao questaoEmEdicao;

    private BancoQuestoes() {
    }

    public static ObservableList<Questao> getQuestoes() {
        return QUESTOES;
    }

    public static void adicionar(Questao questao) {
        if (questao == null) {
            throw new IllegalArgumentException(
                    "A questão não pode ser nula."
            );
        }

        QUESTOES.add(questao);
    }

    public static void prepararEdicao(Questao questao) {
        questaoEmEdicao = questao;
    }

    public static Questao getQuestaoEmEdicao() {
        return questaoEmEdicao;
    }

    public static void limparEdicao() {
        questaoEmEdicao = null;
    }

    public static boolean contem(Questao questao) {
        return QUESTOES.contains(questao);
    }
}