package br.snctech;

import br.snctech.model.Avaliacao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Mostra as avaliações realmente criadas durante a execução. */
public class ProvasAplicadasController {
    @FXML private javafx.scene.control.Button botaoVoltar;
    @FXML private GridPane listaProvas;

    @FXML private void initialize() {
        listaProvas.getChildren().clear();
        int coluna = 0, linha = 0;
        var avaliacoes = DadosAplicacao.getRepositorioAvaliacoes().listarTodas();
        if (avaliacoes.isEmpty()) {
            listaProvas.add(new Label("Nenhuma avaliação cadastrada. Clique em ‘Nova Prova’ para começar."), 0, 0);
            return;
        }
        for (Avaliacao avaliacao : avaliacoes) {
            VBox cartao = new VBox(8);
            cartao.getStyleClass().add("prova-card");
            cartao.getChildren().addAll(new Label(avaliacao.getNome()),
                    new Label("Turma: " + avaliacao.getTurma()),
                    new Label("Data: " + (avaliacao.getData() == null ? "Não definida" : avaliacao.getData())),
                    new Label("Prazo: " + (avaliacao.getPrazo() == null ? "Não definido" : avaliacao.getPrazo())),
                    new Label("Questões: " + avaliacao.getQuestoes().size()),
                    new Label(avaliacao.isRespondida() ? "" : ""));
            cartao.setOnMouseClicked(evento -> {
                ProvaDetalheController.prepararVisualizacao(avaliacao);
                NavegacaoUtil.abrir(cartao, "prova-detalhe.fxml", "SNCTECH - " + avaliacao.getNome());
            });
            listaProvas.add(cartao, coluna, linha);
            coluna++;
            if (coluna == 3) { coluna = 0; linha++; }
        }
    }

    @FXML private void aoClicarNovaProva(ActionEvent evento) {
        NovaProvaController.prepararEdicao(null);
        NavegacaoUtil.abrir((Node) evento.getSource(), "nova-prova.fxml", "SNCTECH - Nova Prova");
    }
    @FXML private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir((Node) evento.getSource(), "dashboard-professor.fxml", "SNCTECH - Professor");
    }
}
