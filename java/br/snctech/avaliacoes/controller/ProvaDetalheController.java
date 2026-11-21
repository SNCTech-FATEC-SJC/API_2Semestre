package br.snctech.avaliacoes.controller;



import br.snctech.util.NavegacaoUtil;
import br.snctech.questoes.model.Questao;
import br.snctech.correcao.DadosCorrecao;
import br.snctech.avaliacoes.model.Avaliacao;
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
    @FXML private javafx.scene.control.Button botaoEditar;
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
        detalhesQuestoes.getChildren().add(new Label("Peso: " + String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.2f", avaliacaoSelecionada.getPeso())));
        if (botaoEditar != null) botaoEditar.setDisable(avaliacaoSelecionada.isDisponibilizada());
        detalhesQuestoes.getChildren().add(new Label("Questões selecionadas:"));
        for (Questao questao : avaliacaoSelecionada.getQuestoes()) {
            Label enunciado = new Label("• " + questao.getEnunciado());
            enunciado.setWrapText(true);
            detalhesQuestoes.getChildren().add(enunciado);
        }
    }
    @FXML private void aoClicarEditar(ActionEvent evento) {
        if (avaliacaoSelecionada != null && avaliacaoSelecionada.isDisponibilizada()) {
            new Alert(Alert.AlertType.WARNING, "Esta prova está ativa e não pode ser editada.")
                    .showAndWait();
            return;
        }
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
