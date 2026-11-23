package br.snctech.alunos.controller;

import br.snctech.alunos.modelo.Aluno;
import br.snctech.alunos.repositorio.AlunoDAO;
import br.snctech.banco.Conexao;
import br.snctech.banco.ErroBanco;
import br.snctech.turmas.modelo.Turma;
import br.snctech.util.DialogoUtil;
import br.snctech.util.NavegacaoUtil;
import br.snctech.util.TabelaUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Gerenciamento de alunos: cadastro, edição, remoção (DELETE) e matrícula em turmas.
 */
public class AlunosController {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @FXML private Button botaoVoltar;
    @FXML private Button botaoNovo;
    @FXML private Button botaoEditar;
    @FXML private Button botaoRemover;
    @FXML private Button botaoTurmas;
    @FXML private TextField campoBusca;
    @FXML private TableView<Aluno> tabela;
    @FXML private Label labelResumo;
    @FXML private Label labelMensagem;

    private final ObservableList<Aluno> alunos = FXCollections.observableArrayList();
    private FilteredList<Aluno> filtrados;

    private record DadosAluno(String nome, String email, String senha) { }

    @FXML
    private void initialize() {
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhum aluno encontrado."));
        tabela.getColumns().add(TabelaUtil.<Aluno>coluna("Aluno", 240, Aluno::nome));
        tabela.getColumns().add(TabelaUtil.<Aluno>coluna("E-mail", 260, Aluno::email));
        tabela.getColumns().add(TabelaUtil.<Aluno>coluna("Turma atual", 200,
                a -> a.turmaAtual() == null || a.turmaAtual().isBlank() ? "-" : a.turmaAtual()));
        tabela.getColumns().add(TabelaUtil.<Aluno>coluna("Cadastrado em", 150, a -> TabelaUtil.data(a.criadoEm())));

        filtrados = new FilteredList<>(alunos, a -> true);
        SortedList<Aluno> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tabela.comparatorProperty());
        tabela.setItems(ordenados);

        campoBusca.textProperty().addListener((obs, antigo, novo) -> aplicarFiltro());
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> atualizarBotoes());

        atualizarBotoes();
        carregar();
    }

    // ------------------------------------------------------------------ dados e filtro

    private void carregar() {
        try {
            Integer selecionadoId = Optional.ofNullable(tabela.getSelectionModel().getSelectedItem())
                    .map(Aluno::id).orElse(null);
            List<Aluno> lista = Conexao.executar(AlunoDAO::listar);
            alunos.setAll(lista);
            if (selecionadoId != null) {
                alunos.stream().filter(a -> a.id() == selecionadoId).findFirst()
                        .ifPresent(a -> tabela.getSelectionModel().select(a));
            }
            labelResumo.setText(lista.size() + " aluno(s) cadastrado(s)");
        } catch (ErroBanco e) {
            mostrarMensagem(e.getMessage(), true);
        }
        atualizarBotoes();
    }

    private void aplicarFiltro() {
        String busca = campoBusca.getText() == null ? "" : campoBusca.getText().trim().toLowerCase();
        filtrados.setPredicate(a -> busca.isEmpty()
                || a.nome().toLowerCase().contains(busca)
                || a.email().toLowerCase().contains(busca)
                || (a.turmaAtual() != null && a.turmaAtual().toLowerCase().contains(busca)));
    }

    private void atualizarBotoes() {
        boolean selecionado = tabela.getSelectionModel().getSelectedItem() != null;
        botaoEditar.setDisable(!selecionado);
        botaoRemover.setDisable(!selecionado);
        botaoTurmas.setDisable(!selecionado);
    }

    private void mostrarMensagem(String texto, boolean erro) {
        labelMensagem.getStyleClass().remove("erro");
        if (erro) {
            labelMensagem.getStyleClass().add("erro");
        }
        labelMensagem.setText(texto);
    }

    private void executarAcao(Runnable acao, String mensagemSucesso) {
        try {
            acao.run();
            carregar();
            mostrarMensagem(mensagemSucesso, false);
        } catch (ErroBanco | IllegalArgumentException e) {
            DialogoUtil.erro(botaoVoltar, traduzir(e));
        }
    }

    /** Mensagem amigável; reconhece a recusa do banco quando o aluno tem histórico de avaliações. */
    private static String traduzir(RuntimeException e) {
        if (e.getCause() instanceof SQLException sql && "23503".equals(sql.getSQLState())) {
            return "Não foi possível excluir: o aluno possui avaliações ou outros registros vinculados.";
        }
        return e.getMessage();
    }

    // ------------------------------------------------------------------ ações

    @FXML
    private void aoClicarNovo(ActionEvent evento) {
        abrirFormulario(null).ifPresent(dados -> executarAcao(
                () -> Conexao.emTransacao(c -> AlunoDAO.cadastrar(c, dados.nome(), dados.email(), dados.senha())),
                "Aluno cadastrado com sucesso."));
    }

    @FXML
    private void aoClicarEditar(ActionEvent evento) {
        Aluno aluno = tabela.getSelectionModel().getSelectedItem();
        if (aluno == null) {
            return;
        }
        abrirFormulario(aluno).ifPresent(dados -> executarAcao(
                () -> Conexao.emTransacao(c ->
                        AlunoDAO.atualizar(c, aluno.id(), dados.nome(), dados.email(), dados.senha())),
                "Aluno atualizado com sucesso."));
    }

    @FXML
    private void aoClicarRemover(ActionEvent evento) {
        Aluno aluno = tabela.getSelectionModel().getSelectedItem();
        if (aluno == null) {
            return;
        }
        if (DialogoUtil.confirmar(botaoVoltar, "Remover aluno",
                "Remover o aluno \"" + aluno.nome() + "\"? Ele será excluído e retirado de todas as turmas. "
                        + "Esta ação não pode ser desfeita.")) {
            executarAcao(() -> Conexao.emTransacao(c -> AlunoDAO.remover(c, aluno.id())),
                    "Aluno removido com sucesso.");
        }
    }

    @FXML
    private void aoClicarTurmas(ActionEvent evento) {
        Aluno aluno = tabela.getSelectionModel().getSelectedItem();
        if (aluno == null) {
            return;
        }
        abrirTurmasDoAluno(aluno);
        carregar();
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrirInicio(botaoVoltar);
    }

    // ------------------------------------------------------------------ formulário do aluno

    private Optional<DadosAluno> abrirFormulario(Aluno existente) {
        boolean novo = existente == null;
        int ignorarId = novo ? 0 : existente.id();

        Dialog<DadosAluno> dialogo = new Dialog<>();
        dialogo.setTitle(novo ? "Novo aluno" : "Editar aluno");
        dialogo.getDialogPane().getButtonTypes().addAll(DialogoUtil.SALVAR, DialogoUtil.CANCELAR);
        DialogoUtil.estilizar(dialogo, botaoVoltar);

        TextField campoNome = new TextField(novo ? "" : existente.nome());
        campoNome.setPromptText("Nome completo");
        campoNome.getStyleClass().add("campo-texto");
        TextField campoEmail = new TextField(novo ? "" : existente.email());
        campoEmail.setPromptText("aluno@email.com");
        campoEmail.getStyleClass().add("campo-texto");
        PasswordField campoSenha = new PasswordField();
        campoSenha.setPromptText(novo ? "Senha de acesso" : "Nova senha (opcional)");
        campoSenha.getStyleClass().add("campo-texto");

        Label dica = new Label(novo ? "A senha é obrigatória no cadastro." : "Deixe a senha em branco para mantê-la.");
        dica.getStyleClass().add("mensagem");
        Label erro = new Label();
        erro.getStyleClass().add("erro");
        erro.setWrapText(true);

        GridPane grade = new GridPane();
        grade.setHgap(12);
        grade.setVgap(10);
        grade.setPadding(new Insets(18));
        ColumnConstraints colunaRotulo = new ColumnConstraints();
        ColumnConstraints colunaCampo = new ColumnConstraints(300);
        colunaCampo.setHgrow(Priority.ALWAYS);
        grade.getColumnConstraints().addAll(colunaRotulo, colunaCampo);
        grade.add(rotulo("Nome"), 0, 0);
        grade.add(campoNome, 1, 0);
        grade.add(rotulo("E-mail"), 0, 1);
        grade.add(campoEmail, 1, 1);
        grade.add(rotulo("Senha"), 0, 2);
        grade.add(campoSenha, 1, 2);
        grade.add(dica, 1, 3);
        grade.add(erro, 0, 4, 2, 1);
        dialogo.getDialogPane().setContent(grade);

        Button botaoSalvar = (Button) dialogo.getDialogPane().lookupButton(DialogoUtil.SALVAR);
        botaoSalvar.addEventFilter(ActionEvent.ACTION, ev -> {
            String mensagem = validar(campoNome.getText().trim(), campoEmail.getText().trim(),
                    campoSenha.getText(), novo, ignorarId);
            if (mensagem != null) {
                erro.setText(mensagem);
                ev.consume();
            }
        });

        dialogo.setResultConverter(botao -> botao == DialogoUtil.SALVAR
                ? new DadosAluno(campoNome.getText().trim(), campoEmail.getText().trim(), campoSenha.getText())
                : null);
        Platform.runLater(campoNome::requestFocus);
        return dialogo.showAndWait();
    }

    private String validar(String nome, String email, String senha, boolean senhaObrigatoria, int ignorarId) {
        if (nome.isEmpty()) {
            return "Informe o nome do aluno.";
        }
        if (nome.length() > 100) {
            return "O nome pode ter no máximo 100 caracteres.";
        }
        if (email.isEmpty()) {
            return "Informe o e-mail do aluno.";
        }
        if (email.length() > 150) {
            return "O e-mail pode ter no máximo 150 caracteres.";
        }
        if (!EMAIL.matcher(email).matches()) {
            return "Informe um e-mail válido.";
        }
        if (senhaObrigatoria && senha.isBlank()) {
            return "Informe a senha do aluno.";
        }
        if (senha.length() > 255) {
            return "A senha pode ter no máximo 255 caracteres.";
        }
        try {
            if (Conexao.executar(c -> AlunoDAO.emailExiste(c, email, ignorarId))) {
                return "Já existe um usuário cadastrado com este e-mail.";
            }
        } catch (ErroBanco e) {
            return e.getMessage();
        }
        return null;
    }

    // ------------------------------------------------------------------ turmas do aluno

    private void abrirTurmasDoAluno(Aluno aluno) {
        TableView<Turma> tabelaTurmas = new TableView<>();
        tabelaTurmas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabelaTurmas.setPlaceholder(new Label("O aluno não está matriculado em nenhuma turma."));
        tabelaTurmas.setPrefHeight(230);

        TableColumn<Turma, String> colunaSituacao = TabelaUtil.<Turma>coluna("Situação", 100, Turma::situacao);
        colunaSituacao.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean vazio) {
                super.updateItem(item, vazio);
                getStyleClass().removeAll("status-ativa", "status-concluida");
                if (vazio || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                getStyleClass().add("Ativa".equals(item) ? "status-ativa" : "status-concluida");
            }
        });
        tabelaTurmas.getColumns().add(TabelaUtil.<Turma>coluna("Turma", 220, Turma::nome));
        tabelaTurmas.getColumns().add(TabelaUtil.<Turma>coluna("Professor", 200, Turma::professorNome));
        tabelaTurmas.getColumns().add(colunaSituacao);

        ComboBox<Turma> comboTurmas = new ComboBox<>();
        comboTurmas.setPromptText("Selecione uma turma ativa");
        comboTurmas.setPrefWidth(360);
        comboTurmas.getStyleClass().add("campo-texto");
        Button botaoAdicionar = new Button("Adicionar à turma");
        botaoAdicionar.getStyleClass().add("botao-primario");
        Button botaoRetirar = new Button("Remover da turma selecionada");
        botaoRetirar.getStyleClass().add("botao-secundario");
        botaoRetirar.disableProperty().bind(tabelaTurmas.getSelectionModel().selectedItemProperty().isNull());

        Label erro = new Label();
        erro.getStyleClass().add("erro");
        erro.setWrapText(true);

        Runnable atualizar = () -> {
            try {
                List<Turma> doAluno = Conexao.executar(c -> AlunoDAO.listarTurmasDoAluno(c, aluno.id()));
                List<Turma> disponiveis = Conexao.executar(c -> AlunoDAO.listarTurmasAtivasDisponiveis(c, aluno.id()));
                tabelaTurmas.setItems(FXCollections.observableArrayList(doAluno));
                comboTurmas.setItems(FXCollections.observableArrayList(disponiveis));
                comboTurmas.getSelectionModel().clearSelection();
                erro.setText("");
            } catch (ErroBanco e) {
                erro.setText(e.getMessage());
            }
        };

        botaoAdicionar.setOnAction(ev -> {
            Turma escolhida = comboTurmas.getValue();
            if (escolhida == null) {
                erro.setText("Selecione uma turma ativa para adicionar o aluno.");
                return;
            }
            try {
                Conexao.emTransacao(c -> AlunoDAO.adicionarATurma(c, aluno.id(), escolhida.id()));
                atualizar.run();
            } catch (ErroBanco | IllegalArgumentException e) {
                erro.setText(e.getMessage());
            }
        });

        botaoRetirar.setOnAction(ev -> {
            Turma selecionada = tabelaTurmas.getSelectionModel().getSelectedItem();
            if (selecionada == null) {
                return;
            }
            if (!selecionada.ativa()) {
                erro.setText("Só é possível remover o aluno de turmas ativas. "
                        + "O histórico das turmas concluídas é preservado.");
                return;
            }
            if (!DialogoUtil.confirmar(botaoVoltar, "Remover da turma",
                    "Remover \"" + aluno.nome() + "\" da turma \"" + selecionada.nome() + "\"?")) {
                return;
            }
            try {
                Conexao.emTransacao(c -> AlunoDAO.removerDaTurma(c, aluno.id(), selecionada.id()));
                atualizar.run();
            } catch (ErroBanco | IllegalArgumentException e) {
                erro.setText(e.getMessage());
            }
        });

        Label titulo = new Label("Turmas de " + aluno.nome());
        titulo.getStyleClass().add("section-label");
        HBox linhaAdicionar = new HBox(10, comboTurmas, botaoAdicionar);
        VBox conteudo = new VBox(12, titulo, tabelaTurmas, linhaAdicionar, botaoRetirar, erro);
        conteudo.setPadding(new Insets(16));
        conteudo.setPrefWidth(660);

        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Turmas do aluno");
        dialogo.getDialogPane().setContent(conteudo);
        dialogo.getDialogPane().getButtonTypes().add(DialogoUtil.FECHAR);
        DialogoUtil.estilizar(dialogo, botaoVoltar);

        atualizar.run();
        dialogo.showAndWait();
    }

    private static Label rotulo(String texto) {
        Label rotulo = new Label(texto);
        rotulo.getStyleClass().add("form-label");
        return rotulo;
    }
}
