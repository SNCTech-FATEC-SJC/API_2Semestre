package br.snctech;

import br.snctech.model.Avaliacao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import java.time.format.DateTimeFormatter;
import javafx.scene.layout.VBox;

public class ProvaDetalheController {
    @FXML private javafx.scene.control.Button botaoVoltar;
    @FXML private Label tituloProva;
    @FXML private VBox detalhesQuestoes;
    private static Avaliacao avaliacaoSelecionada;
    public static void prepararVisualizacao(Avaliacao avaliacao) { avaliacaoSelecionada = avaliacao; }

    @FXML private void initialize() {
        if (avaliacaoSelecionada == null) return;
        tituloProva.setText(avaliacaoSelecionada.getNome());
        detalhesQuestoes.getChildren().clear();
        detalhesQuestoes.getChildren().add(new Label("Turma: " + avaliacaoSelecionada.getTurma()));
        detalhesQuestoes.getChildren().add(new Label("Data: " + (avaliacaoSelecionada.getData() == null ? "Não definida" : avaliacaoSelecionada.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))));
        detalhesQuestoes.getChildren().add(new Label("Prazo: " + (avaliacaoSelecionada.getPrazo() == null ? "Não definido" : avaliacaoSelecionada.getPrazo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))));
        detalhesQuestoes.getChildren().add(new Label("Questões selecionadas:"));
        for (Questao questao : avaliacaoSelecionada.getQuestoes()) {
            detalhesQuestoes.getChildren().add(new Label("• " + questao.getEnunciado()));
        }
    }
    @FXML private void aoClicarEditar(ActionEvent evento) {
        NovaProvaController.prepararEdicao(avaliacaoSelecionada);
        NavegacaoUtil.abrir((Node) evento.getSource(), "nova-prova.fxml", "SNCTECH - Editar Avaliação");
    }
    @FXML private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir((Node) evento.getSource(), "provas-aplicadas.fxml", "SNCTECH - Provas Aplicadas");
    }
}
