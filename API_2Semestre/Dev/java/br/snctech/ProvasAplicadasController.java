package br.snctech;

import br.snctech.model.Avaliacao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.time.format.DateTimeFormatter;

/** Mostra as avaliações realmente criadas durante a execução. */
public class ProvasAplicadasController {
    @FXML private javafx.scene.control.Button botaoVoltar;
    @FXML private GridPane listaProvas;
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private void initialize() {
        atualizarLista();
    }

    private void atualizarLista() {
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
            Label status = new Label(avaliacao.isDisponibilizada()
                    ? "Disponibilizada aos alunos" : "Ainda não disponibilizada");
            Button disponibilizar = new Button(avaliacao.isDisponibilizada()
                    ? "Retirar disponibilização" : "Disponibilizar aos alunos");
            disponibilizar.getStyleClass().add("top-action");
            disponibilizar.setOnAction(evento -> {
                avaliacao.setDisponibilizada(!avaliacao.isDisponibilizada());
                atualizarLista();
            });
            cartao.getChildren().addAll(
                    new Label(avaliacao.getNome()),
                    new Label("Turma(s): " + avaliacao.getTurma()),
                    new Label("Início: " + formatarDataHora(avaliacao, true)),
                    new Label("Prazo: " + formatarDataHora(avaliacao, false)),
                    new Label("Questões: " + avaliacao.getQuestoes().size()),
                    status,
                    disponibilizar
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
            listaProvas.add(cartao, coluna, linha);
            coluna++;
            if (coluna == 3) { coluna = 0; linha++; }
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
    @FXML private void aoClicarVisualizacaoAluno(ActionEvent evento) {
        NavegacaoUtil.abrir((Node) evento.getSource(), "avaliacoes-aluno.fxml", "SNCTECH - Avaliações do Aluno");
    }
    @FXML private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir((Node) evento.getSource(), "dashboard-professor.fxml", "SNCTECH - Professor");
    }
}
