package br.snctech.avaliacoes.controller;

import br.snctech.util.NavegacaoUtil;
import br.snctech.infraestrutura.DadosAplicacao;
import br.snctech.autenticacao.modelo.Perfil;
import br.snctech.autenticacao.modelo.UsuarioLogado;
import br.snctech.banco.SessaoUsuario;
import br.snctech.correcao.model.NotaDoAluno;
import br.snctech.correcao.repositorio.postgres.NotasDoAlunoPostgres;
import br.snctech.notas.controller.PainelMediaDoAluno;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lista as avaliações disponibilizadas e deixa o aluno abrir uma para responder.
 * As questões só aparecem na tela Responder Avaliação, depois que o aluno confirma a abertura.
 */
public class AvaliacoesAlunoController {
    @FXML private VBox listaAvaliacoes;
    @FXML private VBox listaNotas;
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private UsuarioLogado usuarioAtual;

    private final TentativaService servico = new TentativaService(
            DadosAplicacao.getRepositorioTentativas(), DadosAplicacao.getRepositorioAvaliacoes());

    @FXML
    private void initialize() {
        usuarioAtual = SessaoUsuario.getUsuario();
        if (listaNotas != null) {
            mostrarNotas();
        } else {
            atualizarListaAvaliacoes();
        }
    }

    private void atualizarListaAvaliacoes() {
        listaAvaliacoes.getChildren().clear();
        if (usuarioAtual == null) {
            listaAvaliacoes.getChildren().add(mensagem("Faça login para consultar as avaliações."));
            return;
        }
        boolean alunoLogado = usuarioAtual.perfil() == Perfil.ALUNO;
        LocalDateTime agora = LocalDateTime.now();
        String email = usuarioAtual.email();

        java.util.List<Avaliacao> futuras = new java.util.ArrayList<>();
        java.util.List<Avaliacao> disponiveis = new java.util.ArrayList<>();
        java.util.List<Avaliacao> respondidas = new java.util.ArrayList<>();
        java.util.List<Avaliacao> encerradas = new java.util.ArrayList<>();

        for (Avaliacao avaliacao : DadosAplicacao.getRepositorioAvaliacoes().listarTodas()) {
            if (!avaliacao.isDisponibilizada()) continue;
            if (alunoLogado && !DadosAplicacao.getRepositorioTentativas()
                    .alunoMatriculadoNaAvaliacao(avaliacao.getId(), email)) continue;

            LocalDateTime inicio = inicioDa(avaliacao);
            LocalDateTime fim = fimDa(avaliacao);
            if (inicio == null || fim == null) continue;

            Tentativa tentativa = !alunoLogado ? null
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

        adicionarSecao("Avaliações disponíveis", disponiveis, true);
        adicionarSecao("Avaliações futuras", futuras, false);
        adicionarSecao("Avaliações respondidas / em andamento", respondidas, false);
        adicionarSecao("Avaliações encerradas", encerradas, false);
    }

    private void adicionarSecao(String titulo, java.util.List<Avaliacao> avaliacoes, boolean permitirAbrir) {
        Label cabecalho = new Label(titulo);
        cabecalho.getStyleClass().add("section-label");
        listaAvaliacoes.getChildren().add(cabecalho);

        FlowPane grade = new FlowPane();
        grade.setHgap(16);
        grade.setVgap(16);
        grade.getStyleClass().add("card-grid");

        if (avaliacoes.isEmpty()) {
            Label vazio = new Label("Nenhuma avaliação nesta categoria.");
            vazio.getStyleClass().add("mensagem");
            grade.getChildren().add(vazio);
        } else {
            for (Avaliacao avaliacao : avaliacoes) {
                VBox cartao = new VBox(8);
                cartao.getStyleClass().add("prova-card-aluno");
                cartao.setPrefWidth(240);
                cartao.setMaxWidth(300);
                java.util.List<Node> conteudo = new java.util.ArrayList<>();
                conteudo.add(new Label(avaliacao.getNome()));
                conteudo.add(new Label("Turma(s): " + avaliacao.getTurma()));
                conteudo.add(new Label("Início: " + formatarDataHora(avaliacao, true)));
                conteudo.add(new Label("Prazo: " + formatarDataHora(avaliacao, false)));
                conteudo.add(new Label("Questões: " + avaliacao.getQuestoes().size()));
                if (permitirAbrir && usuarioAtual != null && usuarioAtual.perfil() == Perfil.ALUNO) {
                    Button responder = new Button("Responder avaliação");
                    responder.getStyleClass().add("top-action");
                    responder.setOnAction(evento -> abrirAvaliacao(avaliacao, (Node) evento.getSource()));
                    conteudo.add(responder);
                }
                else if ("Avaliações respondidas / em andamento".equals(titulo)) {
                    Tentativa tentativaAtiva = usuarioAtual == null || usuarioAtual.perfil() != Perfil.ALUNO ? null
                            : DadosAplicacao.getRepositorioTentativas().buscarPorAvaliacaoEAluno(avaliacao.getId(), usuarioAtual.email());
                    if (tentativaAtiva != null && tentativaAtiva.isEmAndamento()) {
                        Button retomar = new Button("Retomar avaliação");
                        retomar.getStyleClass().add("top-action");
                        retomar.setOnAction(evento -> retomarAvaliacao(avaliacao, (Node) evento.getSource()));
                        conteudo.add(retomar);
                    }
                }
                cartao.getChildren().addAll(conteudo);
                grade.getChildren().add(cartao);
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
        if (usuarioAtual == null || usuarioAtual.perfil() != Perfil.ALUNO) {
            listaNotas.getChildren().add(mensagem("Entre com uma conta de aluno para consultar suas notas."));
            return;
        }
        String email = usuarioAtual.email();
        listaNotas.getChildren().addAll(PainelMediaDoAluno.linhas(email));
        try {
            var notas = NotasDoAlunoPostgres.listar(email);
            if (notas.isEmpty()) {
                listaNotas.getChildren().add(mensagem("Nenhuma avaliação realizada ou encerrada para este aluno."));
                return;
            }
            for (NotaDoAluno nota : notas) {
                Label linha = new Label(nota.titulo() + "  •  prazo " + nota.prazo().format(FORMATO_DATA_HORA)
                        + "  •  Nota: " + nota.textoDaNota());
                linha.setWrapText(true);
                Button detalhes = new Button("Consultar respostas e correção");
                detalhes.setOnAction(evento -> mostrarDetalhesNota(email, nota.idAvaliacao(), nota.titulo()));
                VBox itemNota = new VBox(5, linha, detalhes);
                listaNotas.getChildren().add(itemNota);
            }
        } catch (RuntimeException erro) {
            listaNotas.getChildren().add(mensagem("Não foi possível carregar as notas. Confira a conexão com o PostgreSQL. "
                    + "Detalhe: " + erro.getMessage()));
        }
    }

    private void mostrarDetalhesNota(String email, int idAvaliacao, String titulo) {
        try {
            var itens = NotasDoAlunoPostgres.detalhar(email, idAvaliacao);
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Resultado da avaliação");
            alerta.setHeaderText(titulo);
            alerta.setContentText(itens.isEmpty() ? "Ainda não há respostas disponíveis para consulta."
                    : String.join("\n\n", itens));
            alerta.getDialogPane().setMinWidth(560);
            alerta.showAndWait();
        } catch (RuntimeException erro) {
            avisar("Não foi possível consultar as respostas: " + erro.getMessage());
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
        if (usuarioAtual == null || usuarioAtual.perfil() != Perfil.ALUNO) {
            avisar("Somente alunos autenticados podem responder avaliações.");
            return;
        }
        String email = usuarioAtual.email();

        String motivo = servico.motivoDeBloqueio(avaliacao.getId(), email);
        if (motivo != null) {
            avisar(motivo);
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Abrir avaliação");
        confirmacao.setHeaderText("Abrir \"" + avaliacao.getNome() + "\" agora?");
        confirmacao.setContentText("Você só pode abrir esta avaliação uma vez. "
                + "Se fechar a janela ou sair sem entregar, a avaliação será encerrada com as respostas já dadas.");
        if (confirmacao.showAndWait().filter(botao -> botao == ButtonType.OK).isEmpty()) {
            return;
        }

        try {
            Tentativa tentativa = servico.iniciar(avaliacao.getId(), email);
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

    private void retomarAvaliacao(Avaliacao avaliacao, Node origem) {
        try {
            if (usuarioAtual == null || usuarioAtual.perfil() != Perfil.ALUNO) {
                avisar("Somente alunos autenticados podem responder avaliações.");
                return;
            }
            Tentativa tentativa = servico.retomar(avaliacao.getId(), usuarioAtual.email());
            ResponderAvaliacaoController.prepararResposta(avaliacao, tentativa);
            NavegacaoUtil.abrir(origem, "responder-avaliacao.fxml", "SNCTECH - " + avaliacao.getNome());
        } catch (RuntimeException erro) {
            avisar(erro.getMessage());
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
        if (listaNotas != null) {
            NavegacaoUtil.abrir((Node) evento.getSource(), "dashboard-aluno.fxml", "SNCTECH - Aluno");
        } else {
            boolean professor = usuarioAtual != null && usuarioAtual.perfil() == Perfil.PROFESSOR;
            NavegacaoUtil.abrir((Node) evento.getSource(),
                    professor ? "dashboard-professor.fxml" : "dashboard-aluno.fxml",
                    professor ? "SNCTECH - Professor" : "SNCTECH - Aluno");
        }
    }

}
