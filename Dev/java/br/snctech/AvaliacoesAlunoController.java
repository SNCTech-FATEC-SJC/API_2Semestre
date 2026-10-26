package br.snctech;

import br.snctech.correcao.model.NotaDoAluno;
import br.snctech.correcao.repositorio.postgres.NotasDoAlunoPostgres;
import br.snctech.model.Avaliacao;
import br.snctech.model.Tentativa;
import br.snctech.service.TentativaService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lista as avaliações disponibilizadas e deixa o aluno abrir uma para responder.
 * As questões só aparecem na tela Responder Avaliação, depois que o aluno confirma a abertura.
 */
public class AvaliacoesAlunoController {
    @FXML private GridPane listaAvaliacoes;
    @FXML private TextField campoAluno;
    @FXML private VBox listaNotas;
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    // Provisório até existir login: o aluno informa o e-mail que já está cadastrado no banco.
    private static String alunoAtual = "";

    private final TentativaService servico = new TentativaService(
            DadosAplicacao.getRepositorioTentativas(), DadosAplicacao.getRepositorioAvaliacoes());

    @FXML
    private void initialize() {
        campoAluno.setText(alunoAtual);
        mostrarNotas();
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
            cartao.getStyleClass().add("prova-card-aluno");
            Button responder = new Button("Responder avaliação");
            responder.getStyleClass().add("top-action");
            responder.setOnAction(evento -> abrirAvaliacao(avaliacao, (Node) evento.getSource()));
            cartao.getChildren().addAll(
                    new Label(avaliacao.getNome()),
                    new Label("Turma(s): " + avaliacao.getTurma()),
                    new Label("Início: " + formatarDataHora(avaliacao, true)),
                    new Label("Prazo: " + formatarDataHora(avaliacao, false)),
                    new Label("Questões: " + avaliacao.getQuestoes().size()),
                    new Label("Duração: " + (avaliacao.getDuracaoMinutos() > 0
                            ? avaliacao.getDuracaoMinutos() + " min" : "até o prazo")),
                    responder
            );
            listaAvaliacoes.add(cartao, coluna, linha);
            coluna++;
            if (coluna == 3) {
                coluna = 0;
                linha++;
            }
        }
    }

    /**
     * PB-11: mostra as avaliações já realizadas (ou encerradas) do aluno com a nota.
     * A nota só aparece depois da correção completa; antes disso aparece "Em correção".
     */
    @FXML
    private void mostrarNotas() {
        listaNotas.getChildren().clear();
        String email = campoAluno.getText() == null ? "" : campoAluno.getText().trim();
        if (email.isEmpty()) {
            listaNotas.getChildren().add(mensagem("Informe o e-mail do aluno e clique em \"Ver minhas notas\"."));
            return;
        }
        try {
            alunoAtual = email;
            var notas = NotasDoAlunoPostgres.listar(email);
            if (notas.isEmpty()) {
                listaNotas.getChildren().add(mensagem("Nenhuma avaliação realizada ou encerrada para este aluno."));
                return;
            }
            for (NotaDoAluno nota : notas) {
                Label linha = new Label(nota.titulo() + "  •  prazo " + nota.prazo().format(FORMATO_DATA_HORA)
                        + "  •  Nota: " + nota.textoDaNota());
                linha.setWrapText(true);
                listaNotas.getChildren().add(linha);
            }
        } catch (RuntimeException erro) {
            listaNotas.getChildren().add(mensagem("Não foi possível carregar as notas. Confira a conexão com o PostgreSQL. "
                    + "Detalhe: " + erro.getMessage()));
        }
    }

    private Label mensagem(String texto) {
        Label label = new Label(texto);
        label.setWrapText(true);
        label.getStyleClass().add("mensagem");
        return label;
    }

    /** Confere as regras, pede confirmação (só dá para abrir uma vez) e vai para a tela de resposta. */
    private void abrirAvaliacao(Avaliacao avaliacao, Node origem) {
        alunoAtual = campoAluno.getText() == null ? "" : campoAluno.getText().trim();

        String motivo = servico.motivoDeBloqueio(avaliacao.getId(), alunoAtual);
        if (motivo != null) {
            avisar(motivo);
            campoAluno.requestFocus();
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Abrir avaliação");
        confirmacao.setHeaderText("Abrir \"" + avaliacao.getNome() + "\" agora?");
        confirmacao.setContentText("Você só pode abrir esta avaliação uma vez. "
                + (avaliacao.getDuracaoMinutos() > 0
                ? "O tempo de " + avaliacao.getDuracaoMinutos() + " minutos começa a contar agora. "
                : "")
                + "Se fechar a janela ou sair sem entregar, a avaliação será encerrada com as respostas já dadas.");
        if (confirmacao.showAndWait().filter(botao -> botao == ButtonType.OK).isEmpty()) {
            return;
        }

        try {
            Tentativa tentativa = servico.iniciar(avaliacao.getId(), alunoAtual);
            ResponderAvaliacaoController.prepararResposta(avaliacao, tentativa);
            NavegacaoUtil.abrir(origem, "responder-avaliacao.fxml", "SNCTECH - " + avaliacao.getNome());
        } catch (IllegalArgumentException erro) {
            avisar(erro.getMessage());
        } catch (RuntimeException erro) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro ao abrir avaliação");
            alerta.setHeaderText("Não foi possível iniciar a avaliação");
            alerta.setContentText("Confira a conexão com o PostgreSQL e tente novamente. Detalhe: " + erro.getMessage());
            alerta.showAndWait();
        }
    }

    private void avisar(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Não foi possível abrir a avaliação");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
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
