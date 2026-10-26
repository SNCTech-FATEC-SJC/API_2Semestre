package br.snctech;

import br.snctech.model.Avaliacao;
import br.snctech.model.Tentativa;
import br.snctech.service.ApoioResposta;
import br.snctech.service.TentativaService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tela em que o aluno responde a avaliação (PB-06 e PB-07).
 * Toda a regra (tempo, uma única tentativa, respostas mantidas ao encerrar) está no TentativaService;
 * aqui só montamos as questões, mostramos o cronômetro e repassamos o que o aluno faz.
 * Cada resposta é registrada no momento em que o aluno marca/digita, por isso, se o tempo acabar
 * ou ele fechar a janela, tudo o que já respondeu continua guardado.
 */
public class ResponderAvaliacaoController {
    @FXML private Label tituloProva;
    @FXML private Label rotuloAluno;
    @FXML private Label cronometro;
    @FXML private Label mensagem;
    @FXML private VBox listaQuestoes;
    @FXML private Button botaoSair;
    @FXML private Button botaoEntregar;

    private static Avaliacao avaliacaoAtual;
    private static Tentativa tentativaAtual;

    /** Chamado pela tela de avaliações do aluno logo depois de TentativaService.iniciar. */
    public static void prepararResposta(Avaliacao avaliacao, Tentativa tentativa) {
        avaliacaoAtual = avaliacao;
        tentativaAtual = tentativa;
    }

    private final TentativaService servico = new TentativaService(
            DadosAplicacao.getRepositorioTentativas(), DadosAplicacao.getRepositorioAvaliacoes());
    private final List<Control> camposDeResposta = new ArrayList<>();
    private Timeline relogio;
    private Stage palco;
    private boolean travada = false;

    @FXML
    private void initialize() {
        if (avaliacaoAtual == null || tentativaAtual == null) {
            tituloProva.setText("Nenhuma avaliação aberta");
            rotuloAluno.setText("");
            cronometro.setText("");
            mensagem.setText("Volte para a lista e abra uma avaliação.");
            botaoEntregar.setDisable(true);
            botaoSair.setText("Voltar para as avaliações");
            return;
        }

        tituloProva.setText(avaliacaoAtual.getNome());
        rotuloAluno.setText("Aluno: " + tentativaAtual.getAluno());
        montarQuestoes();
        atualizarCronometro();

        if (!travada) {
            relogio = new Timeline(new KeyFrame(Duration.seconds(1), evento -> atualizarCronometro()));
            relogio.setCycleCount(Timeline.INDEFINITE);
            relogio.play();
            // A cena só existe depois que a tela termina de carregar.
            Platform.runLater(this::registrarFechamentoDaJanela);
        }
    }

    // ---------------------------------------------------------------- questões

    private void montarQuestoes() {
        int numero = 1;
        for (Questao questao : avaliacaoAtual.getQuestoes()) {
            VBox cartao = new VBox(8);
            cartao.getStyleClass().add("resposta-card");

            Label titulo = new Label("Questão " + numero++);
            titulo.getStyleClass().add("section-label");
            Label enunciado = new Label(questao.getEnunciado());
            enunciado.setWrapText(true);
            cartao.getChildren().addAll(titulo, enunciado);
            adicionarImagem(cartao, questao);

            String tipo = questao.getTipo() == null ? "" : questao.getTipo();
            switch (tipo) {
                case "Múltipla Escolha" -> montarEscolhaUnica(cartao, questao, questao.getAlternativas());
                case "Verdadeiro/Falso" -> montarEscolhaUnica(cartao, questao, List.of("Verdadeiro", "Falso"));
                case "Ligar Colunas" -> montarLigarColunas(cartao, questao);
                default -> montarDissertativa(cartao, questao);
            }
            listaQuestoes.getChildren().add(cartao);
        }
    }

    /** Múltipla escolha e verdadeiro/falso: uma opção por vez; a resposta é o texto da opção marcada. */
    private void montarEscolhaUnica(VBox cartao, Questao questao, List<String> opcoes) {
        ToggleGroup grupo = new ToggleGroup();
        for (String opcao : opcoes) {
            RadioButton botao = new RadioButton(opcao);
            botao.setWrapText(true);
            botao.setToggleGroup(grupo);
            botao.setOnAction(evento -> salvarResposta(questao, opcao));
            camposDeResposta.add(botao);
            cartao.getChildren().add(botao);
        }
    }

    private void montarDissertativa(VBox cartao, Questao questao) {
        TextArea campo = new TextArea();
        campo.setPromptText("Digite sua resposta");
        campo.setWrapText(true);
        campo.setPrefRowCount(4);
        // Registra a cada alteração: o que foi digitado antes do limite de tempo não se perde.
        campo.textProperty().addListener((observado, antes, depois) -> salvarResposta(questao, depois));
        camposDeResposta.add(campo);
        cartao.getChildren().add(campo);
    }

    /** Para cada termo da coluna A o aluno escolhe a correspondência da coluna B (embaralhada). */
    private void montarLigarColunas(VBox cartao, Questao questao) {
        List<Questao.ParColuna> pares = questao.getPares();
        List<String> opcoes = new ArrayList<>();
        for (Questao.ParColuna par : pares) opcoes.add(par.getTermoB());
        Collections.shuffle(opcoes);

        List<ComboBox<String>> escolhas = new ArrayList<>();
        for (Questao.ParColuna par : pares) {
            Label termo = new Label(par.getTermoA());
            termo.setMinWidth(160);
            termo.setWrapText(true);
            ComboBox<String> escolha = new ComboBox<>();
            escolha.getItems().addAll(opcoes);
            escolha.setPromptText("Escolha a correspondência");
            escolha.setOnAction(evento -> {
                List<String> valores = new ArrayList<>();
                for (ComboBox<String> outra : escolhas) valores.add(outra.getValue());
                salvarResposta(questao, ApoioResposta.montarRespostaColunas(pares, valores));
            });
            escolhas.add(escolha);
            camposDeResposta.add(escolha);
            HBox linha = new HBox(10, termo, escolha);
            linha.setAlignment(Pos.CENTER_LEFT);
            cartao.getChildren().add(linha);
        }
    }

    private void adicionarImagem(VBox cartao, Questao questao) {
        String caminho = questao.getImagemPath();
        if (caminho == null || caminho.isBlank()) return;
        File arquivo = new File(caminho);
        if (!arquivo.isFile()) return;
        try {
            Image imagem = new Image(arquivo.toURI().toString(), 420, 0, true, true);
            if (!imagem.isError()) cartao.getChildren().add(new ImageView(imagem));
        } catch (Exception erro) {
            // A imagem é um complemento: se não carregar, a questão continua podendo ser respondida.
        }
    }

    // ---------------------------------------------------------------- respostas e tempo

    private void salvarResposta(Questao questao, String resposta) {
        if (travada) return;
        try {
            servico.responder(tentativaAtual.getId(), questao, resposta);
            mensagem.setText("");
        } catch (IllegalArgumentException erro) {
            if (!tentativaAtual.isEmAndamento()) {
                tempoEsgotado(); // o tempo acabou entre um tique do cronômetro e outro
            } else {
                mensagem.setText(erro.getMessage());
            }
        } catch (br.snctech.banco.ErroBanco erro) {
            // Uma falha no banco não pode derrubar o JavaFX nem fingir que a resposta foi salva.
            mensagem.setText("Falha ao salvar sua resposta no banco. A tela foi bloqueada para evitar novas alterações. Detalhe: "
                    + erro.getMessage());
            travarTela();
        }
    }

    private void atualizarCronometro() {
        if (travada) return;
        long restante = servico.segundosRestantes(tentativaAtual.getId());
        if (!tentativaAtual.isEmAndamento()) {
            tempoEsgotado();
            return;
        }
        cronometro.setText("Tempo restante: " + ApoioResposta.formatarTempo(restante));
        if (restante <= 60 && !cronometro.getStyleClass().contains("cronometro-alerta")) {
            cronometro.getStyleClass().add("cronometro-alerta");
        }
    }

    private void tempoEsgotado() {
        if (travada) return;
        travarTela();
        cronometro.setText("Tempo esgotado");
        mensagem.setText("O tempo acabou. As respostas dadas até aqui foram salvas e a avaliação foi encerrada.");
        botaoSair.setText("Voltar para as avaliações");
    }

    private void travarTela() {
        travada = true;
        if (relogio != null) relogio.stop();
        for (Control campo : camposDeResposta) campo.setDisable(true);
        botaoEntregar.setDisable(true);
    }

    // ---------------------------------------------------------------- saída da tela

    @FXML
    private void aoClicarEntregar(ActionEvent evento) {
        if (travada) return;
        if (!confirmar("Entregar avaliação", "Deseja entregar a avaliação agora?",
                "Depois de entregar, não será possível alterar as respostas.")) {
            return;
        }
        Tentativa resultado = servico.finalizar(tentativaAtual.getId());
        travarTela();
        if (resultado.getStatus() == Tentativa.Status.FINALIZADA) {
            informar("Avaliação entregue", "Suas respostas foram registradas com sucesso.");
        } else {
            informar("Tempo esgotado", "O tempo acabou antes da entrega, mas as respostas dadas até o limite foram salvas.");
        }
        voltarParaAsAvaliacoes(evento);
    }

    @FXML
    private void aoClicarSair(ActionEvent evento) {
        if (tentativaAtual != null && !travada) {
            if (!confirmar("Sair da avaliação", "Deseja sair sem entregar?",
                    "A avaliação será encerrada com as respostas já registradas e você não poderá abri-la novamente.")) {
                return;
            }
            servico.encerrar(tentativaAtual.getId());
            travarTela();
        }
        voltarParaAsAvaliacoes(evento);
    }

    /** Fechar a janela com a prova aberta encerra a tentativa, mantendo as respostas já registradas. */
    private void registrarFechamentoDaJanela() {
        if (cronometro.getScene() == null) return;
        palco = (Stage) cronometro.getScene().getWindow();
        palco.setOnCloseRequest(evento -> {
            if (tentativaAtual != null && tentativaAtual.isEmAndamento()) {
                servico.encerrar(tentativaAtual.getId());
            }
        });
    }

    private void voltarParaAsAvaliacoes(ActionEvent evento) {
        if (relogio != null) relogio.stop();
        if (palco != null) palco.setOnCloseRequest(null);
        avaliacaoAtual = null;
        tentativaAtual = null;
        NavegacaoUtil.abrir((Node) evento.getSource(), "avaliacoes-aluno.fxml", "SNCTECH - Avaliações do Aluno");
    }

    private boolean confirmar(String titulo, String cabecalho, String texto) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecalho);
        alerta.setContentText(texto);
        return alerta.showAndWait().filter(botao -> botao == ButtonType.OK).isPresent();
    }

    private void informar(String titulo, String texto) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(texto);
        alerta.showAndWait();
    }
}
