package br.snctech;

import br.snctech.model.Avaliacao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Lista apenas os dados das avaliações disponibilizadas, sem expor suas questões. */
public class AvaliacoesAlunoController {
    @FXML private GridPane listaAvaliacoes;
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    @FXML
    private void initialize() {
        listaAvaliacoes.getChildren().clear();
        int coluna = 0;
        int linha = 0;
        LocalDateTime agora = LocalDateTime.now();
        var avaliacoes = DadosAplicacao.getRepositorioAvaliacoes().listarTodas().stream()
                .filter(Avaliacao::isDisponibilizada)
                .filter(avaliacao -> avaliacao.getPrazo() != null
                        && avaliacao.getHoraFim() != null
                        && !avaliacao.getPrazo().atTime(avaliacao.getHoraFim()).isBefore(agora))
                .toList();

        if (avaliacoes.isEmpty()) {
            Label vazio = new Label("Nenhuma avaliação disponível ou dentro do prazo.");
            vazio.setWrapText(true);
            vazio.getStyleClass().add("mensagem");
            listaAvaliacoes.add(vazio, 0, 0, 3, 1);
            return;
        }

        for (Avaliacao avaliacao : avaliacoes) {
            VBox cartao = new VBox(8);
            cartao.getStyleClass().add("prova-card");
            cartao.getChildren().addAll(
                    new Label(avaliacao.getNome()),
                    new Label("Turma(s): " + avaliacao.getTurma()),
                    new Label("Início: " + formatarDataHora(avaliacao, true)),
                    new Label("Prazo: " + formatarDataHora(avaliacao, false)),
                    new Label("As questões ainda não estão disponíveis para visualização.")
            );
            listaAvaliacoes.add(cartao, coluna, linha);
            coluna++;
            if (coluna == 3) {
                coluna = 0;
                linha++;
            }
        }
    }

    private String formatarDataHora(Avaliacao avaliacao, boolean inicio) {
        var data = inicio ? avaliacao.getData() : avaliacao.getPrazo();
        var hora = inicio ? avaliacao.getHoraInicio() : avaliacao.getHoraFim();
        return data == null ? "Não definida" : data.atTime(hora).format(FORMATO_DATA_HORA);
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir((Node) evento.getSource(), "provas-aplicadas.fxml", "SNCTECH - Banco de Provas");
    }
}
