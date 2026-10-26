package br.snctech;

import br.snctech.model.Avaliacao;
import br.snctech.service.AvaliacaoService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Controla criação/edição e carrega questões do módulo da outra dupla. */
public class NovaProvaController {
    @FXML private TextField campoNome;
    @FXML private TextField campoTurma;
    @FXML private DatePicker campoData;
    @FXML private DatePicker campoPrazo;
    @FXML private TextField campoHoraInicio;
    @FXML private TextField campoHoraFim;
    @FXML private TextField campoDuracao;
    @FXML private VBox listaQuestoes;
    @FXML private javafx.scene.control.Button botaoVoltar;
    private final List<CheckBox> seletores = new ArrayList<>();
    private static Avaliacao avaliacaoEmEdicao;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("H:mm");

    public static void prepararEdicao(Avaliacao avaliacao) { avaliacaoEmEdicao = avaliacao; }

    @FXML
    private void initialize() {
        // Monta a lista usando BancoQuestoes, em vez das cinco questões estáticas do FXML.
        for (Questao questao : DadosAplicacao.getRepositorioQuestoes().listarTodas()) {
            CheckBox seletor = new CheckBox(questao.getEnunciado());
            seletor.setWrapText(true);
            seletor.setUserData(questao);
            seletores.add(seletor);
            VBox cartao = new VBox(6, seletor, new Label("Tipo: " + questao.getTipo()));
            cartao.getStyleClass().add("question-card");
            listaQuestoes.getChildren().add(cartao);
        }

        // Padrões de uma avaliação nova: o dia inteiro e sem limite de tempo por aluno.
        campoHoraInicio.setText("00:00");
        campoHoraFim.setText("23:59");
        campoDuracao.setText("0");

        if (avaliacaoEmEdicao != null) {
            campoHoraInicio.setText(avaliacaoEmEdicao.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")));
            campoHoraFim.setText(avaliacaoEmEdicao.getHoraFim().format(DateTimeFormatter.ofPattern("HH:mm")));
            campoDuracao.setText(String.valueOf(avaliacaoEmEdicao.getDuracaoMinutos()));
            campoNome.setText(avaliacaoEmEdicao.getNome());
            campoTurma.setText(avaliacaoEmEdicao.getTurma());
            campoData.setValue(avaliacaoEmEdicao.getData());
            campoPrazo.setValue(avaliacaoEmEdicao.getPrazo());
            for (CheckBox seletor : seletores) {
                if (avaliacaoEmEdicao.getQuestoes().contains(seletor.getUserData())) seletor.setSelected(true);
            }
        }
    }

    @FXML
    private void aoClicarSalvar(ActionEvent evento) {
        // Primeiro recolhemos as questões marcadas pelo professor.
        List<Questao> selecionadas = new ArrayList<>();
        for (CheckBox seletor : seletores) {
            if (seletor.isSelected()) {
                selecionadas.add((Questao) seletor.getUserData());
            }
        }

        // Validamos os campos da tela antes de chamar o serviço.
        // Assim, se faltar uma data ou prazo, mostramos uma mensagem e
        // permanecemos na tela para o professor corrigir o campo.
        if (campoNome.getText() == null || campoNome.getText().isBlank()) {
            mostrarAviso("Informe o nome da avaliação.", campoNome);
            return;
        }
        if (campoTurma.getText() == null || campoTurma.getText().isBlank()) {
            mostrarAviso("Informe a turma da avaliação.", campoTurma);
            return;
        }
        if (selecionadas.isEmpty()) {
            mostrarAviso("Selecione pelo menos uma questão.", seletores.isEmpty() ? null : seletores.get(0));
            return;
        }
        if (campoData.getValue() == null) {
            mostrarAviso("Selecione a data da avaliação.", campoData);
            return;
        }
        if (campoPrazo.getValue() == null) {
            mostrarAviso("Selecione o prazo de entrega.", campoPrazo);
            return;
        }
        if (campoPrazo.getValue().isBefore(campoData.getValue())) {
            mostrarAviso("O prazo de entrega não pode ser anterior à data da avaliação.", campoPrazo);
            return;
        }

        LocalTime horaInicio = lerHora(campoHoraInicio.getText());
        if (horaInicio == null) {
            mostrarAviso("Informe a hora de início no formato HH:mm (ex.: 08:00).", campoHoraInicio);
            return;
        }
        LocalTime horaFim = lerHora(campoHoraFim.getText());
        if (horaFim == null) {
            mostrarAviso("Informe a hora de término no formato HH:mm (ex.: 23:59).", campoHoraFim);
            return;
        }
        int duracao;
        try {
            String textoDuracao = campoDuracao.getText() == null ? "" : campoDuracao.getText().trim();
            duracao = textoDuracao.isEmpty() ? 0 : Integer.parseInt(textoDuracao);
        } catch (NumberFormatException erro) {
            mostrarAviso("Informe a duração em minutos, só com números (use 0 para não limitar).", campoDuracao);
            return;
        }

        try {
            AvaliacaoService servico = new AvaliacaoService(DadosAplicacao.getRepositorioAvaliacoes());
            if (avaliacaoEmEdicao == null) {
                servico.criar(campoNome.getText(), selecionadas, campoTurma.getText(), campoData.getValue(), campoPrazo.getValue(),
                        horaInicio, horaFim, duracao);
            } else {
                servico.editar(avaliacaoEmEdicao.getId(), campoNome.getText(), selecionadas, campoTurma.getText(), campoData.getValue(), campoPrazo.getValue(),
                        horaInicio, horaFim, duracao);
            }
            avaliacaoEmEdicao = null;
            NavegacaoUtil.abrir((Node) evento.getSource(), "provas-aplicadas.fxml", "SNCTECH - Provas Aplicadas");
        } catch (IllegalArgumentException erro) {
            // Regras de negócio também são validadas no serviço, como segunda proteção.
            mostrarAviso(erro.getMessage(), null);
        } catch (Exception erro) {
            // Evita que uma falha inesperada pare sem explicação para o usuário.
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro ao salvar avaliação");
            alerta.setHeaderText("Não foi possível concluir o salvamento");
            alerta.setContentText("Ocorreu um erro inesperado. Confira os dados e tente novamente.\nDetalhe: " + erro.getMessage());
            alerta.showAndWait();
        }
    }

    /** Lê "8:00" ou "08:00". Devolve null se o texto não for uma hora válida. */
    private LocalTime lerHora(String texto) {
        if (texto == null) return null;
        try {
            return LocalTime.parse(texto.trim(), FORMATO_HORA);
        } catch (DateTimeParseException erro) {
            return null;
        }
    }

    /** Exibe uma mensagem clara e devolve o foco ao campo que precisa ser corrigido. */
    private void mostrarAviso(String mensagem, javafx.scene.Node campo) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Não foi possível salvar");
        alerta.setHeaderText("Verifique os dados da avaliação");
        alerta.setContentText(mensagem);
        alerta.showAndWait();
        if (campo != null) {
            campo.requestFocus();
        }
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        avaliacaoEmEdicao = null;
        NavegacaoUtil.abrir((Node) evento.getSource(), "provas-aplicadas.fxml", "SNCTECH - Provas Aplicadas");
    }
}
