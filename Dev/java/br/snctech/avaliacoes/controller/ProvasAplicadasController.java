package br.snctech.avaliacoes.controller;


import br.snctech.util.NavegacaoUtil;
import br.snctech.infraestrutura.DadosAplicacao;
import br.snctech.avaliacoes.model.Avaliacao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.time.format.DateTimeFormatter;

/** Mostra as avaliações realmente criadas durante a execução. */
public class ProvasAplicadasController {
    @FXML private javafx.scene.control.Button botaoVoltar;
    @FXML private FlowPane listaProvas;
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private void initialize() {
        atualizarLista();
    }

    private void atualizarLista() {
        listaProvas.getChildren().clear();
        var avaliacoes = DadosAplicacao.getRepositorioAvaliacoes().listarTodas();
        if (avaliacoes.isEmpty()) {
            listaProvas.getChildren().add(new Label("Nenhuma avaliação cadastrada."));
            return;
        }
        for (Avaliacao avaliacao : avaliacoes) {
            VBox cartao = new VBox(8);
            cartao.getStyleClass().add("prova-card");
            Label status = new Label(avaliacao.isDisponibilizada()
                    ? "Disponibilizada aos alunos" : "Ainda não disponibilizada");
            Button disponibilizar = new Button(avaliacao.isDisponibilizada()
                    ? "Retirar disponibilização" : "Disponibilizar aos alunos");
            disponibilizar.getStyleClass().add("top-action");
            disponibilizar.setOnAction(evento -> {
                boolean novoEstado = !avaliacao.isDisponibilizada();
                try {
                    // Importante: não basta alterar o objeto em memória. O repositório
                    // também grava liberada/liberada_em no PostgreSQL quando o modo banco está ativo.
                    DadosAplicacao.getRepositorioAvaliacoes()
                            .atualizarDisponibilizacao(avaliacao, novoEstado);
                    atualizarLista();
                } catch (RuntimeException erro) {
                    Alert alerta = new Alert(Alert.AlertType.ERROR);
                    alerta.setTitle("Erro ao atualizar avaliação");
                    alerta.setHeaderText("Não foi possível alterar a disponibilização.");
                    alerta.setContentText(erro.getMessage() == null
                            ? "Confira a conexão com o PostgreSQL e tente novamente."
                            : erro.getMessage());
                    alerta.showAndWait();
                }
            });
            Button excluir = new Button("Excluir prova");
            excluir.getStyleClass().add("delete-action");
            excluir.setOnAction(evento -> {
                Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacao.setTitle("Excluir prova");
                confirmacao.setHeaderText("Excluir \"" + avaliacao.getNome() + "\"?");
                confirmacao.setContentText("A prova e os dados de respostas associados serão removidos permanentemente.");
                if (confirmacao.showAndWait().filter(botao -> botao == javafx.scene.control.ButtonType.OK).isEmpty()) return;
                try {
                    DadosAplicacao.getRepositorioAvaliacoes().excluir(avaliacao.getId());
                    atualizarLista();
                } catch (RuntimeException erro) {
                    Alert alerta = new Alert(Alert.AlertType.ERROR);
                    alerta.setTitle("Erro ao excluir prova");
                    alerta.setHeaderText("Não foi possível excluir a avaliação.");
                    alerta.setContentText(erro.getMessage() == null
                            ? "Confira a conexão com o PostgreSQL e tente novamente."
                            : erro.getMessage());
                    alerta.showAndWait();
                }
            });
            cartao.getChildren().addAll(
                    new Label(avaliacao.getNome()),
                    new Label("Turma(s): " + avaliacao.getTurma()),
                    new Label("Início: " + formatarDataHora(avaliacao, true)),
                    new Label("Prazo: " + formatarDataHora(avaliacao, false)),
                    new Label("Questões: " + avaliacao.getQuestoes().size()),
                    new Label("Alunos que abriram: " + DadosAplicacao.getRepositorioTentativas().listarPorAvaliacao(avaliacao.getId()).size()),
                    status,
                    disponibilizar,
                    excluir
            );
            cartao.setOnMouseClicked(evento -> {
                Node alvo = (Node) evento.getTarget();
                while (alvo != null && alvo != cartao) {
                    if (alvo instanceof Button) return;
                    alvo = alvo.getParent();
                }
                ProvaDetalheController.prepararVisualizacao(avaliacao);
                NavegacaoUtil.abrir(cartao, "prova-detalhe.fxml", "SNCTECH - " + avaliacao.getNome());
            });
            cartao.setPrefWidth(230);
            cartao.setMaxWidth(280);
            listaProvas.getChildren().add(cartao);
        }
    }

    private String formatarDataHora(Avaliacao avaliacao, boolean inicio) {
        var data = inicio ? avaliacao.getData() : avaliacao.getPrazo();
        var hora = inicio ? avaliacao.getHoraInicio() : avaliacao.getHoraFim();
        return data == null ? "Não definida" : data.atTime(hora).format(FORMATO_DATA_HORA);
    }

    @FXML private void aoClicarNovaProva(ActionEvent evento) {
        NovaProvaController.prepararEdicao(null);
        NavegacaoUtil.abrir((Node) evento.getSource(), "nova-prova.fxml", "SNCTECH - Nova Prova");
    }
    @FXML private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir((Node) evento.getSource(), "dashboard-professor.fxml", "SNCTECH - Professor");
    }
}
