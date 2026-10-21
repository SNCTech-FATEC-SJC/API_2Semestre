package br.snctech;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class BancoQuestoesController {

    @FXML
    private Button botaoVoltar;

    @FXML
    private GridPane gradeQuestoes;

    @FXML
    private void initialize() {
        atualizarQuestoes();
    }

    @FXML
    private void aoClicarAdicionarQuestao(ActionEvent evento) {
        BancoQuestoes.limparEdicao();

        Node origem = (Node) evento.getSource();

        NavegacaoUtil.abrir(
                origem,
                "questoes.fxml",
                "SNCTECH - Questões"
        );
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir(
                botaoVoltar,
                "dashboard-professor.fxml",
                "SNCTECH - Professor"
        );
    }

    private void atualizarQuestoes() {
        gradeQuestoes.getChildren().clear();

        int coluna = 0;
        int linha = 0;

        for (int i = 0; i < BancoQuestoes.getQuestoes().size(); i++) {
            Questao questao = BancoQuestoes.getQuestoes().get(i);

            gradeQuestoes.add(
                    criarCartao(questao, i + 1),
                    coluna,
                    linha
            );

            coluna++;

            if (coluna == 5) {
                coluna = 0;
                linha++;
            }
        }

        if (BancoQuestoes.getQuestoes().isEmpty()) {
            Label vazio = new Label(
                    "Nenhuma questão cadastrada. "
                    + "Clique em Adicionar Questão para começar."
            );

            vazio.setWrapText(true);
            vazio.getStyleClass().add("mensagem");

            gradeQuestoes.add(vazio, 0, 0, 5, 1);
        }
    }

    private VBox criarCartao(Questao questao, int numero) {
        VBox cartao = new VBox(8);
        cartao.getStyleClass().add("question-card");

        Label titulo = new Label("Questão " + numero);
        titulo.getStyleClass().add("title");

        Label tipo = new Label(questao.getTipo());
        tipo.getStyleClass().add("subtitle");

        Label enunciado = new Label(questao.getEnunciado());
        enunciado.getStyleClass().add("subtitle");
        enunciado.setWrapText(true);
        enunciado.setMaxWidth(180);

        String resposta;

        switch (questao.getTipo()) {
            case "Múltipla Escolha":
                resposta = "Alternativa correta: "
                        + questao.getRespostaCorreta();
                break;

            case "Verdadeiro/Falso":
                resposta = "Resposta: "
                        + questao.getRespostaCorreta();
                break;

            case "Dissertativa":
                resposta = "Resposta esperada cadastrada";
                break;

            case "Ligar Colunas":
                resposta = "Pares correspondentes: "
                        + questao.getPares().size();
                break;

            default:
                resposta = "Gabarito cadastrado";
        }

        Label detalhe = new Label(resposta);
        detalhe.getStyleClass().add("subtitle");
        detalhe.setWrapText(true);

        Button editar = new Button("Editar");
        editar.getStyleClass().add("botao-secundario");
        editar.setMaxWidth(Double.MAX_VALUE);

        editar.setOnAction(event -> abrirEdicao(questao, editar));

        cartao.getChildren().addAll(
                titulo,
                tipo,
                enunciado,
                detalhe,
                editar
        );

        return cartao;
    }

    private void abrirEdicao(Questao questao, Node origem) {
        BancoQuestoes.prepararEdicao(questao);

        NavegacaoUtil.abrir(
                origem,
                "questoes.fxml",
                "SNCTECH - Editar Questão"
        );
    }
}