package br.snctech.avaliacoes.controller;



import br.snctech.util.NavegacaoUtil;
import br.snctech.infraestrutura.DadosAplicacao;
import br.snctech.questoes.model.Questao;
import br.snctech.avaliacoes.model.Avaliacao;
import br.snctech.avaliacoes.service.AvaliacaoService;
import br.snctech.banco.Conexao;
import br.snctech.banco.SessaoUsuario;
import br.snctech.turmas.modelo.Turma;
import br.snctech.turmas.repositorio.TurmaDAO;
import br.snctech.turmas.modelo.Membro;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Controla criação/edição e carrega questões do módulo da outra dupla. */
public class NovaProvaController {
    @FXML private TextField campoNome;
    @FXML private VBox listaTurmas;
    @FXML private VBox listaAlunos;
    @FXML private DatePicker campoData;
    @FXML private DatePicker campoPrazo;
    @FXML private TextField campoHoraInicio;
    @FXML private TextField campoHoraFim;
    @FXML private TextField campoPeso;
    @FXML private VBox listaQuestoes;
    @FXML private javafx.scene.control.Button botaoVoltar;
    private final List<CheckBox> seletores = new ArrayList<>();
    private final List<CheckBox> seletoresTurma = new ArrayList<>();
    private final List<CheckBox> seletoresAluno = new ArrayList<>();
    private final Map<Integer, CheckBox> caixasAluno = new LinkedHashMap<>();
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
        campoPeso.setText("1,0");

        List<Turma> turmas = Conexao.executar(
                conexao -> TurmaDAO.listar(conexao, SessaoUsuario.getProfessorId()));
        for (Turma turma : turmas) {
            CheckBox caixa = new CheckBox(turma.nome());
            caixa.setUserData(turma);
            caixa.setOnAction(evento -> atualizarListaAlunos());
            seletoresTurma.add(caixa);
            listaTurmas.getChildren().add(caixa);
        }
        atualizarListaAlunos();
        if (avaliacaoEmEdicao != null) {
            campoHoraInicio.setText(avaliacaoEmEdicao.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")));
            campoHoraFim.setText(avaliacaoEmEdicao.getHoraFim().format(DateTimeFormatter.ofPattern("HH:mm")));
            campoPeso.setText(String.valueOf(avaliacaoEmEdicao.getPeso()).replace('.', ','));
            campoNome.setText(avaliacaoEmEdicao.getNome());
            for (CheckBox caixa : seletoresTurma) {
                Turma turma = (Turma) caixa.getUserData();
                caixa.setSelected(avaliacaoEmEdicao.getTurma().contains(turma.nome()));
            }
            atualizarListaAlunos();
            for (CheckBox caixa : seletoresAluno) {
                Membro aluno = (Membro) caixa.getUserData();
                caixa.setSelected(avaliacaoEmEdicao.getAlunosSelecionados().contains(aluno.alunoId()));
            }
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
        List<Turma> turmasSelecionadas = new ArrayList<>();
        for (CheckBox caixa : seletoresTurma) {
            if (caixa.isSelected()) turmasSelecionadas.add((Turma) caixa.getUserData());
        }
        if (turmasSelecionadas.isEmpty()) {
            mostrarAviso("Selecione pelo menos uma turma.", seletoresTurma.isEmpty() ? null : seletoresTurma.get(0));
            return;
        }
        List<Integer> alunosSelecionados = new ArrayList<>();
        for (CheckBox caixa : seletoresAluno) {
            if (caixa.isSelected()) alunosSelecionados.add(((Membro) caixa.getUserData()).alunoId());
        }
        if (alunosSelecionados.isEmpty()) {
            mostrarAviso("Selecione pelo menos um aluno.", seletoresAluno.isEmpty() ? null : seletoresAluno.get(0));
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
        double peso;
        try {
            String textoPeso = campoPeso.getText() == null ? "" : campoPeso.getText().trim().replace(',', '.');
            peso = Double.parseDouble(textoPeso);
        } catch (NumberFormatException erro) {
            mostrarAviso("Informe um peso numérico válido (ex.: 1,0).", campoPeso);
            return;
        }

        try {
            AvaliacaoService servico = new AvaliacaoService(DadosAplicacao.getRepositorioAvaliacoes());
            String turma = turmasSelecionadas.stream().map(Turma::nome).collect(java.util.stream.Collectors.joining(", "));
            Avaliacao resultado;
            if (avaliacaoEmEdicao == null) {
                resultado = servico.criar(campoNome.getText(), selecionadas, turma, campoData.getValue(), campoPrazo.getValue(),
                        horaInicio, horaFim, 0, peso);
            } else {
                resultado = servico.editar(avaliacaoEmEdicao.getId(), campoNome.getText(), selecionadas, turma, campoData.getValue(), campoPrazo.getValue(),
                        horaInicio, horaFim, 0, peso);
            }
            DadosAplicacao.getRepositorioAvaliacoes().configurarAlunos(resultado, alunosSelecionados);
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

    private void atualizarListaAlunos() {
        Set<Integer> selecionados = new LinkedHashSet<>();
        for (CheckBox caixa : seletoresAluno) {
            if (caixa.isSelected()) selecionados.add(((Membro) caixa.getUserData()).alunoId());
        }
        listaAlunos.getChildren().clear();
        seletoresAluno.clear();
        caixasAluno.clear();
        for (CheckBox caixaTurma : seletoresTurma) {
            if (!caixaTurma.isSelected()) continue;
            Turma turma = (Turma) caixaTurma.getUserData();
            List<Membro> membros = Conexao.executar(conexao -> TurmaDAO.listarMembros(conexao, turma.id()));
            for (Membro membro : membros) {
                if (caixasAluno.containsKey(membro.alunoId())) continue;
                CheckBox caixaAluno = new CheckBox(membro.nome() + " (" + membro.email() + ")");
                caixaAluno.setUserData(membro);
                caixaAluno.setSelected(selecionados.contains(membro.alunoId()));
                caixasAluno.put(membro.alunoId(), caixaAluno);
                seletoresAluno.add(caixaAluno);
                listaAlunos.getChildren().add(caixaAluno);
            }
        }
    }

    @FXML
    private void aoClicarSelecionarTodosAlunos() {
        for (CheckBox caixa : seletoresAluno) caixa.setSelected(true);
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
