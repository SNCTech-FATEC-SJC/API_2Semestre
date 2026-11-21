package br.snctech.turmas.controller;

import br.snctech.autenticacao.modelo.Perfil;
import br.snctech.autenticacao.modelo.UsuarioLogado;
import br.snctech.banco.Conexao;
import br.snctech.banco.ErroBanco;
import br.snctech.banco.SessaoUsuario;
import br.snctech.turmas.modelo.AvaliacaoTurma;
import br.snctech.turmas.modelo.Membro;
import br.snctech.turmas.modelo.Professor;
import br.snctech.turmas.modelo.Turma;
import br.snctech.turmas.repositorio.TurmaDAO;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

/**
 * Gerenciamento de turmas: cadastro e edição, turmas ativas x concluídas, membros e
 * avaliação das turmas concluídas.
 * O administrador vê todas as turmas; o professor vê apenas as suas.
 */
public class TurmasController {

    private static final String TODAS = "Todas as turmas";
    private static final String ATIVAS = "Turmas ativas";
    private static final String CONCLUIDAS = "Turmas concluídas";

    @FXML private Button botaoVoltar;
    @FXML private Button botaoNova;
    @FXML private Button botaoEditar;
    @FXML private Button botaoAlternar;
    @FXML private Button botaoMembros;
    @FXML private Button botaoAvaliacao;
    @FXML private TextField campoBusca;
    @FXML private ComboBox<String> filtroStatus;
    @FXML private TableView<Turma> tabela;
    @FXML private Label labelResumo;
    @FXML private Label labelMensagem;

    private final ObservableList<Turma> turmas = FXCollections.observableArrayList();
    private FilteredList<Turma> filtradas;
    private int usuarioId;
    private boolean administrador;

    private record DadosTurma(String nome, int professorId) { }

    @FXML
    private void initialize() {
        configurarTabela();

        filtroStatus.setItems(FXCollections.observableArrayList(TODAS, ATIVAS, CONCLUIDAS));
        filtroStatus.getSelectionModel().select(TODAS);

        filtradas = new FilteredList<>(turmas, t -> true);
        SortedList<Turma> ordenadas = new SortedList<>(filtradas);
        ordenadas.comparatorProperty().bind(tabela.comparatorProperty());
        tabela.setItems(ordenadas);

        campoBusca.textProperty().addListener((obs, antigo, novo) -> aplicarFiltro());
        filtroStatus.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltro());
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> atualizarBotoes());

        UsuarioLogado logado = SessaoUsuario.getUsuario();
        usuarioId = logado != null ? logado.id() : -1;
        administrador = logado != null && logado.perfil() == Perfil.ADMIN;

        atualizarBotoes();
        carregar();
    }

    private void configurarTabela() {
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhuma turma encontrada."));

        TableColumn<Turma, String> colunaSituacao = TabelaUtil.<Turma>coluna("Situação", 110, Turma::situacao);
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

        tabela.getColumns().add(TabelaUtil.<Turma>coluna("Turma", 240, Turma::nome));
        tabela.getColumns().add(TabelaUtil.<Turma>coluna("Professor responsável", 220, Turma::professorNome));
        tabela.getColumns().add(colunaSituacao);
        tabela.getColumns().add(TabelaUtil.<Turma>coluna("Alunos", 80, t -> String.valueOf(t.totalAlunos())));
        tabela.getColumns().add(TabelaUtil.<Turma>coluna("Criada em", 150, t -> TabelaUtil.data(t.criadoEm())));
    }

    // ------------------------------------------------------------------ dados e filtro

    private void carregar() {
        try {
            Integer filtroProfessor = administrador ? null : usuarioId;
            List<Turma> lista = Conexao.executar(c -> TurmaDAO.listar(c, filtroProfessor));
            Integer selecionadaId = Optional.ofNullable(tabela.getSelectionModel().getSelectedItem())
                    .map(Turma::id).orElse(null);
            turmas.setAll(lista);
            if (selecionadaId != null) {
                turmas.stream().filter(t -> t.id() == selecionadaId).findFirst()
                        .ifPresent(t -> tabela.getSelectionModel().select(t));
            }
            long ativas = lista.stream().filter(Turma::ativa).count();
            labelResumo.setText(lista.size() + " turma(s): " + ativas + " ativa(s) e "
                    + (lista.size() - ativas) + " concluída(s)");
        } catch (ErroBanco e) {
            mostrarMensagem(e.getMessage(), true);
        }
        atualizarBotoes();
    }

    private void aplicarFiltro() {
        String busca = campoBusca.getText() == null ? "" : campoBusca.getText().trim().toLowerCase();
        String status = filtroStatus.getValue();
        filtradas.setPredicate(t -> {
            boolean passaStatus = status == null || TODAS.equals(status)
                    || (ATIVAS.equals(status) && t.ativa())
                    || (CONCLUIDAS.equals(status) && !t.ativa());
            boolean passaTexto = busca.isEmpty()
                    || t.nome().toLowerCase().contains(busca)
                    || (t.professorNome() != null && t.professorNome().toLowerCase().contains(busca));
            return passaStatus && passaTexto;
        });
    }

    private void atualizarBotoes() {
        Turma turma = tabela.getSelectionModel().getSelectedItem();
        boolean selecionada = turma != null;
        botaoEditar.setDisable(!selecionada);
        botaoMembros.setDisable(!selecionada);
        botaoAlternar.setDisable(!selecionada);
        botaoAvaliacao.setDisable(!selecionada || turma.ativa());
        botaoAlternar.setText(selecionada && !turma.ativa() ? "Reativar turma" : "Concluir turma");
    }

    private void mostrarMensagem(String texto, boolean erro) {
        labelMensagem.getStyleClass().remove("erro");
        if (erro) {
            labelMensagem.getStyleClass().add("erro");
        }
        labelMensagem.setText(texto);
    }

    private boolean podeGerenciar(Turma turma) {
        return administrador || turma.professorId() == usuarioId;
    }

    /** Executa uma gravação, recarrega a tabela e mostra o resultado. */
    private void executarAcao(Runnable acao, String mensagemSucesso) {
        try {
            acao.run();
            carregar();
            mostrarMensagem(mensagemSucesso, false);
        } catch (ErroBanco | IllegalArgumentException e) {
            DialogoUtil.erro(botaoVoltar, e.getMessage());
        }
    }

    // ------------------------------------------------------------------ ações

    @FXML
    private void aoClicarNova(ActionEvent evento) {
        abrirFormulario(null).ifPresent(dados -> executarAcao(
                () -> Conexao.emTransacao(c -> TurmaDAO.cadastrar(c, dados.nome(), dados.professorId())),
                "Turma cadastrada com sucesso."));
    }

    @FXML
    private void aoClicarEditar(ActionEvent evento) {
        Turma turma = tabela.getSelectionModel().getSelectedItem();
        if (turma == null) {
            return;
        }
        if (!podeGerenciar(turma)) {
            DialogoUtil.erro(botaoVoltar, "Apenas o professor responsável ou o administrador pode editar esta turma.");
            return;
        }
        abrirFormulario(turma).ifPresent(dados -> executarAcao(
                () -> Conexao.emTransacao(c -> TurmaDAO.atualizar(c, turma.id(), dados.nome(), dados.professorId())),
                "Turma atualizada com sucesso."));
    }

    @FXML
    private void aoClicarAlternarStatus(ActionEvent evento) {
        Turma turma = tabela.getSelectionModel().getSelectedItem();
        if (turma == null) {
            return;
        }
        if (!podeGerenciar(turma)) {
            DialogoUtil.erro(botaoVoltar,
                    "Apenas o professor responsável ou o administrador pode concluir ou reativar esta turma.");
            return;
        }
        if (turma.ativa()) {
            if (DialogoUtil.confirmar(botaoVoltar, "Concluir turma",
                    "Concluir a turma \"" + turma.nome() + "\"? Ela passará para o histórico de turmas concluídas.")) {
                executarAcao(() -> Conexao.emTransacao(c -> TurmaDAO.concluir(c, turma.id())),
                        "Turma concluída com sucesso.");
            }
        } else if (DialogoUtil.confirmar(botaoVoltar, "Reativar turma",
                "Reativar a turma \"" + turma.nome() + "\"? Ela voltará para a lista de turmas ativas.")) {
            executarAcao(() -> Conexao.emTransacao(c -> TurmaDAO.reativar(c, turma.id())),
                    "Turma reativada com sucesso.");
        }
    }

    @FXML
    private void aoClicarMembros(ActionEvent evento) {
        Turma turma = tabela.getSelectionModel().getSelectedItem();
        if (turma == null) {
            return;
        }
        List<Membro> membros;
        try {
            membros = Conexao.executar(c -> TurmaDAO.listarMembros(c, turma.id()));
        } catch (ErroBanco e) {
            DialogoUtil.erro(botaoVoltar, e.getMessage());
            return;
        }

        TableView<Membro> tabelaMembros = new TableView<>(FXCollections.observableArrayList(membros));
        tabelaMembros.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabelaMembros.setPlaceholder(new Label("Esta turma ainda não possui alunos."));
        tabelaMembros.setPrefHeight(300);
        tabelaMembros.getColumns().add(TabelaUtil.<Membro>coluna("Aluno", 220, Membro::nome));
        tabelaMembros.getColumns().add(TabelaUtil.<Membro>coluna("E-mail", 240, Membro::email));
        tabelaMembros.getColumns().add(TabelaUtil.<Membro>coluna("Matriculado em", 140, m -> TabelaUtil.data(m.matriculadoEm())));

        Label titulo = new Label(turma.nome() + " - " + turma.situacao());
        titulo.getStyleClass().add("section-label");
        Label total = new Label(membros.size() + " aluno(s) na turma. Para adicionar ou remover alunos, "
                + "use o Gerenciamento de Alunos.");
        total.getStyleClass().add("mensagem");

        VBox conteudo = new VBox(10, titulo, total, tabelaMembros);
        conteudo.setPadding(new Insets(16));
        conteudo.setPrefWidth(640);

        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Membros da turma");
        dialogo.getDialogPane().setContent(conteudo);
        dialogo.getDialogPane().getButtonTypes().add(DialogoUtil.FECHAR);
        DialogoUtil.estilizar(dialogo, botaoVoltar);
        dialogo.showAndWait();
    }

    @FXML
    private void aoClicarAvaliacao(ActionEvent evento) {
        Turma turma = tabela.getSelectionModel().getSelectedItem();
        if (turma == null) {
            return;
        }
        if (turma.ativa()) {
            DialogoUtil.informar(botaoVoltar, "A avaliação só fica disponível para turmas concluídas.");
            return;
        }
        List<AvaliacaoTurma> avaliacoes;
        try {
            avaliacoes = Conexao.executar(c -> TurmaDAO.listarAvaliacoes(c, turma.id()));
        } catch (ErroBanco e) {
            DialogoUtil.erro(botaoVoltar, e.getMessage());
            return;
        }

        long concluidas = avaliacoes.stream().filter(AvaliacaoTurma::concluidaPelaTurma).count();
        String resumo;
        if (avaliacoes.isEmpty()) {
            resumo = "Nenhuma avaliação foi aplicada a esta turma.";
        } else if (concluidas == avaliacoes.size()) {
            resumo = "A turma concluiu todas as avaliações (" + concluidas + ").";
        } else {
            resumo = "A turma concluiu " + concluidas + " de " + avaliacoes.size() + " avaliações.";
        }

        TableView<AvaliacaoTurma> tabelaAvaliacoes = new TableView<>(FXCollections.observableArrayList(avaliacoes));
        tabelaAvaliacoes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabelaAvaliacoes.setPlaceholder(new Label("Sem avaliações."));
        tabelaAvaliacoes.setPrefHeight(260);
        tabelaAvaliacoes.getColumns().add(TabelaUtil.<AvaliacaoTurma>coluna("Avaliação", 260, AvaliacaoTurma::titulo));
        tabelaAvaliacoes.getColumns().add(TabelaUtil.<AvaliacaoTurma>coluna("Alunos que enviaram", 140,
                a -> a.concluiram() + " de " + a.totalAlunos()));
        tabelaAvaliacoes.getColumns().add(TabelaUtil.<AvaliacaoTurma>coluna("Situação", 180, AvaliacaoTurma::situacao));

        Label titulo = new Label("Avaliação da turma " + turma.nome());
        titulo.getStyleClass().add("section-label");
        Label rotuloResumo = new Label(resumo);
        rotuloResumo.getStyleClass().add("section-label");
        rotuloResumo.setWrapText(true);

        VBox conteudo = new VBox(10, titulo, rotuloResumo, tabelaAvaliacoes);
        conteudo.setPadding(new Insets(16));
        conteudo.setPrefWidth(640);

        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Avaliação da turma");
        dialogo.getDialogPane().setContent(conteudo);
        dialogo.getDialogPane().getButtonTypes().add(DialogoUtil.FECHAR);
        DialogoUtil.estilizar(dialogo, botaoVoltar);
        dialogo.showAndWait();
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrirInicio(botaoVoltar);
    }

    // ------------------------------------------------------------------ formulário

    private Optional<DadosTurma> abrirFormulario(Turma existente) {
        List<Professor> professores;
        try {
            professores = Conexao.executar(TurmaDAO::listarProfessores);
        } catch (ErroBanco e) {
            DialogoUtil.erro(botaoVoltar, e.getMessage());
            return Optional.empty();
        }

        Dialog<DadosTurma> dialogo = new Dialog<>();
        dialogo.setTitle(existente == null ? "Nova turma" : "Editar turma");
        dialogo.getDialogPane().getButtonTypes().addAll(DialogoUtil.SALVAR, DialogoUtil.CANCELAR);
        DialogoUtil.estilizar(dialogo, botaoVoltar);

        TextField campoNome = new TextField(existente == null ? "" : existente.nome());
        campoNome.setPromptText("Ex.: 3º Ano DSM");
        campoNome.getStyleClass().add("campo-texto");

        ComboBox<Professor> comboProfessor = new ComboBox<>(FXCollections.observableArrayList(professores));
        comboProfessor.setMaxWidth(Double.MAX_VALUE);
        comboProfessor.getStyleClass().add("campo-texto");
        int professorPadrao = existente != null ? existente.professorId() : usuarioId;
        professores.stream().filter(p -> p.id() == professorPadrao).findFirst()
                .ifPresent(p -> comboProfessor.getSelectionModel().select(p));
        comboProfessor.setDisable(!administrador);

        Label dica = new Label(existente == null
                ? "A turma é criada como ativa. Um professor pode ter várias turmas ativas ao mesmo tempo."
                : "Um professor pode ter várias turmas ativas ao mesmo tempo.");
        dica.getStyleClass().add("mensagem");
        dica.setWrapText(true);
        Label erro = new Label();
        erro.getStyleClass().add("erro");
        erro.setWrapText(true);

        GridPane grade = new GridPane();
        grade.setHgap(12);
        grade.setVgap(10);
        grade.setPadding(new Insets(18));
        ColumnConstraints colunaRotulo = new ColumnConstraints();
        ColumnConstraints colunaCampo = new ColumnConstraints(280);
        colunaCampo.setHgrow(Priority.ALWAYS);
        grade.getColumnConstraints().addAll(colunaRotulo, colunaCampo);
        grade.add(rotulo("Nome da turma"), 0, 0);
        grade.add(campoNome, 1, 0);
        grade.add(rotulo("Professor responsável"), 0, 1);
        grade.add(comboProfessor, 1, 1);
        grade.add(dica, 0, 2, 2, 1);
        grade.add(erro, 0, 3, 2, 1);
        dialogo.getDialogPane().setContent(grade);

        Button botaoSalvar = (Button) dialogo.getDialogPane().lookupButton(DialogoUtil.SALVAR);
        botaoSalvar.addEventFilter(ActionEvent.ACTION, ev -> {
            String nome = campoNome.getText().trim();
            String mensagem = null;
            if (nome.isEmpty()) {
                mensagem = "Informe o nome da turma.";
            } else if (nome.length() > 100) {
                mensagem = "O nome da turma pode ter no máximo 100 caracteres.";
            } else if (comboProfessor.getValue() == null) {
                mensagem = "Selecione o professor responsável.";
            }
            if (mensagem != null) {
                erro.setText(mensagem);
                ev.consume();
            }
        });

        dialogo.setResultConverter(botao -> botao == DialogoUtil.SALVAR
                ? new DadosTurma(campoNome.getText().trim(), comboProfessor.getValue().id())
                : null);
        Platform.runLater(campoNome::requestFocus);
        return dialogo.showAndWait();
    }

    private static Label rotulo(String texto) {
        Label rotulo = new Label(texto);
        rotulo.getStyleClass().add("form-label");
        return rotulo;
    }
}
