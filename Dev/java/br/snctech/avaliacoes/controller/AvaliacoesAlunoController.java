package br.snctech.avaliacoes.controller;


import br.snctech.alunos.repositorio.AlunoDAO;
import br.snctech.autenticacao.modelo.Perfil;
import br.snctech.autenticacao.modelo.UsuarioLogado;
import br.snctech.banco.Conexao;
import br.snctech.banco.ErroBanco;
import br.snctech.banco.SessaoUsuario;
import br.snctech.turmas.modelo.Turma;
import br.snctech.util.NavegacaoUtil;
import br.snctech.infraestrutura.DadosAplicacao;
import br.snctech.correcao.model.NotaDoAluno;
import br.snctech.correcao.repositorio.postgres.NotasDoAlunoPostgres;
import br.snctech.avaliacoes.model.Avaliacao;
import br.snctech.avaliacoes.model.Tentativa;
import br.snctech.avaliacoes.service.TentativaService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.VBox;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lista as avaliações disponibilizadas e deixa o aluno abrir uma para responder.
 * As questões só aparecem na tela Responder Avaliação, depois que o aluno confirma a abertura.
 */
public class AvaliacoesAlunoController {
    @FXML private VBox listaAvaliacoes;
    @FXML private TextField campoAluno;
    @FXML private VBox listaNotas;
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    // Logado como ALUNO: vale o e-mail do login. Professor/administrador (visualização do aluno): informam o e-mail.
    private static String alunoAtual = "";

    private final TentativaService servico = new TentativaService(
            DadosAplicacao.getRepositorioTentativas(), DadosAplicacao.getRepositorioAvaliacoes());

    @FXML
    private void initialize() {
        UsuarioLogado usuario = SessaoUsuario.getUsuario();
        boolean logadoComoAluno = usuario != null && usuario.perfil() == Perfil.ALUNO;
        if (logadoComoAluno) {
            alunoAtual = usuario.email();
            campoAluno.setText(alunoAtual);
            campoAluno.setEditable(false);
            campoAluno.setFocusTraversable(false);
        } else {
            campoAluno.setText(alunoAtual);
        }
        mostrarNotas();
        listaAvaliacoes.getChildren().clear();

        final Set<Integer> turmasPermitidas;
        try {
            turmasPermitidas = logadoComoAluno
                    ? Conexao.executar(c -> AlunoDAO.listarTurmasDoAluno(c, usuario.id())).stream()
                            .map(Turma::id).collect(Collectors.toSet())
                    : null;
        } catch (ErroBanco erro) {
            listaAvaliacoes.getChildren().add(
                    mensagem("Não foi possível carregar suas turmas. Detalhe: " + erro.getMessage()));
            return;
        }

        LocalDateTime agora = LocalDateTime.now();
        String email = campoAluno.getText() == null ? "" : campoAluno.getText().trim();

        java.util.List<Avaliacao> futuras = new java.util.ArrayList<>();
        java.util.List<Avaliacao> disponiveis = new java.util.ArrayList<>();
        java.util.List<Avaliacao> respondidas = new java.util.ArrayList<>();
        java.util.List<Avaliacao> encerradas = new java.util.ArrayList<>();

        for (Avaliacao avaliacao : DadosAplicacao.getRepositorioAvaliacoes().listarTodas()) {
            if (!avaliacao.isDisponibilizada()) continue;
            if (turmasPermitidas != null && !turmasPermitidas.contains(avaliacao.getTurmaId())) continue;

            LocalDateTime inicio = inicioDa(avaliacao);
            LocalDateTime fim = fimDa(avaliacao);
            if (inicio == null || fim == null) continue;

            Tentativa tentativa = email.isBlank() ? null
                    : DadosAplicacao.getRepositorioTentativas()
                            .buscarPorAvaliacaoEAluno(avaliacao.getId(), email);

            if (tentativa != null) {
                if (tentativa.isEmAndamento() && agora.isBefore(tentativa.getLimite())) respondidas.add(avaliacao);
                else if (tentativa.getStatus() == Tentativa.Status.FINALIZADA) respondidas.add(avaliacao);
                else encerradas.add(avaliacao);
            } else if (agora.isBefore(inicio)) {
                futuras.add(avaliacao);
            } else if (!agora.isBefore(fim)) {
                encerradas.add(avaliacao);
            } else {
                disponiveis.add(avaliacao);
            }
        }

        adicionarSecao("Avaliações disponíveis", disponiveis, agora, true);
        adicionarSecao("Avaliações futuras", futuras, agora, false);
        adicionarSecao("Avaliações respondidas / em andamento", respondidas, agora, false);
        adicionarSecao("Avaliações encerradas", encerradas, agora, false);
    }

    private void adicionarSecao(String titulo, java.util.List<Avaliacao> avaliacoes,
                                LocalDateTime agora, boolean permitirAbrir) {
        Label cabecalho = new Label(titulo);
        cabecalho.getStyleClass().add("section-label");
        listaAvaliacoes.getChildren().add(cabecalho);

        GridPane grade = new GridPane();
        grade.setHgap(16);
        grade.setVgap(16);
        grade.getStyleClass().add("card-grid");
        for (int i = 0; i < 3; i++) {
            ColumnConstraints coluna = new ColumnConstraints();
            coluna.setPercentWidth(100.0 / 3.0);
            grade.getColumnConstraints().add(coluna);
        }

        if (avaliacoes.isEmpty()) {
            Label vazio = new Label("Nenhuma avaliação nesta categoria.");
            vazio.getStyleClass().add("mensagem");
            grade.add(vazio, 0, 0);
        } else {
            int coluna = 0;
            int linha = 0;
            for (Avaliacao avaliacao : avaliacoes) {
                VBox cartao = new VBox(8);
                cartao.getStyleClass().add("prova-card-aluno");
                java.util.List<Node> conteudo = new java.util.ArrayList<>();
                conteudo.add(new Label(avaliacao.getNome()));
                conteudo.add(new Label("Turma(s): " + avaliacao.getTurma()));
                conteudo.add(new Label("Início: " + formatarDataHora(avaliacao, true)));
                conteudo.add(new Label("Prazo: " + formatarDataHora(avaliacao, false)));
                conteudo.add(new Label("Questões: " + avaliacao.getQuestoes().size()));
                conteudo.add(new Label("Duração: " + (avaliacao.getDuracaoMinutos() > 0
                        ? avaliacao.getDuracaoMinutos() + " min" : "até o prazo")));
                if (permitirAbrir) {
                    Button responder = new Button("Responder avaliação");
                    responder.getStyleClass().add("top-action");
                    responder.setOnAction(evento -> abrirAvaliacao(avaliacao, (Node) evento.getSource()));
                    conteudo.add(responder);
                }
                cartao.getChildren().addAll(conteudo);
                grade.add(cartao, coluna, linha);
                coluna++;
                if (coluna == 3) {
                    coluna = 0;
                    linha++;
                }
            }
        }
        listaAvaliacoes.getChildren().add(grade);
    }

    /**
     * PB-11: mostra as avaliações já realizadas (ou encerradas) do aluno com a nota.
     * A nota só aparece depois da correção completa; antes disso aparece "Em correção".
     */
    @FXML
    private void mostrarNotas() {
        listaNotas.getChildren().clear();
        String email = emailDoAluno();
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

    /** Aluno logado: sempre o e-mail do login (não dá para trocar na tela). Demais perfis: o campo digitado. */
    private String emailDoAluno() {
        UsuarioLogado usuario = SessaoUsuario.getUsuario();
        if (usuario != null && usuario.perfil() == Perfil.ALUNO) {
            return usuario.email();
        }
        return campoAluno.getText() == null ? "" : campoAluno.getText().trim();
    }

    private Label mensagem(String texto) {
        Label label = new Label(texto);
        label.setWrapText(true);
        label.getStyleClass().add("mensagem");
        return label;
    }

    /** Confere as regras, pede confirmação (só dá para abrir uma vez) e vai para a tela de resposta. */
    private void abrirAvaliacao(Avaliacao avaliacao, Node origem) {
        alunoAtual = emailDoAluno();

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

    private LocalDateTime inicioDa(Avaliacao avaliacao) {
        return avaliacao.getData() == null || avaliacao.getHoraInicio() == null
                ? null : avaliacao.getData().atTime(avaliacao.getHoraInicio());
    }

    private LocalDateTime fimDa(Avaliacao avaliacao) {
        return avaliacao.getPrazo() == null || avaliacao.getHoraFim() == null
                ? null : avaliacao.getPrazo().atTime(avaliacao.getHoraFim());
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        UsuarioLogado usuario = SessaoUsuario.getUsuario();
        if (usuario != null && usuario.perfil() == Perfil.ALUNO) {
            NavegacaoUtil.abrirInicio((Node) evento.getSource());
        } else {
            NavegacaoUtil.abrir((Node) evento.getSource(), "provas-aplicadas.fxml", "SNCTECH - Banco de Provas");
        }
    }
}
