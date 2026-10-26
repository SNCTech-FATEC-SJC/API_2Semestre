package br.snctech;

import br.snctech.correcao.DadosCorrecao;
import br.snctech.model.Avaliacao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
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
        detalhesQuestoes.getChildren().add(new Label("Horário: "
                + avaliacaoSelecionada.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")) + " às "
                + avaliacaoSelecionada.getHoraFim().format(DateTimeFormatter.ofPattern("HH:mm"))));
        detalhesQuestoes.getChildren().add(new Label("Duração por aluno: " + (avaliacaoSelecionada.getDuracaoMinutos() > 0
                ? avaliacaoSelecionada.getDuracaoMinutos() + " minutos" : "sem limite além do prazo")));
        detalhesQuestoes.getChildren().add(new Label("Questões selecionadas:"));
        for (Questao questao : avaliacaoSelecionada.getQuestoes()) {
            detalhesQuestoes.getChildren().add(new Label("• " + questao.getEnunciado()));
        }
    }
    @FXML private void aoClicarEditar(ActionEvent evento) {
        NovaProvaController.prepararEdicao(avaliacaoSelecionada);
        NavegacaoUtil.abrir((Node) evento.getSource(), "nova-prova.fxml", "SNCTECH - Editar Avaliação");
    }
    /** PB-03 / PB-08: abre a correção desta avaliação (provas entregues, correção automática e manual). */
    @FXML private void aoClicarCorrigir(ActionEvent evento) {
        if (avaliacaoSelecionada == null || avaliacaoSelecionada.getId() <= 0) return;
        try {
            DadosCorrecao.prepararCorrecao(avaliacaoSelecionada.getId(), avaliacaoSelecionada.getNome(),
                    avaliacaoSelecionada.getTurma(),
                    origem -> NavegacaoUtil.abrir(origem, "prova-detalhe.fxml", "SNCTECH - Detalhes da Avaliação"));
            NavegacaoUtil.abrir((Node) evento.getSource(), "correcao-alunos.fxml", "SNCTECH - Correção da prova");
        } catch (RuntimeException e) {
            new Alert(Alert.AlertType.ERROR, "Não foi possível abrir a correção.\n" + e.getMessage()).showAndWait();
        }
    }
    @FXML private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir((Node) evento.getSource(), "provas-aplicadas.fxml", "SNCTECH - Provas Aplicadas");
    }
}
