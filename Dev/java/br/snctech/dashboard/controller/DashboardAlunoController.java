package br.snctech.dashboard.controller;

import br.snctech.autenticacao.modelo.UsuarioLogado;
import br.snctech.autenticacao.modelo.Perfil;
import br.snctech.banco.SessaoUsuario;
import br.snctech.infraestrutura.DadosAplicacao;
import br.snctech.avaliacoes.model.Avaliacao;
import br.snctech.util.NavegacaoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardAlunoController {

    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    @FXML private Label labelUsuario;
    @FXML private Label labelBoasVindas;
    @FXML private VBox listaProvasDisponibilizadas;
    @FXML private Button botaoTurmas;
    @FXML private Button botaoAvaliacoes;
    @FXML private Button botaoNotas;
    @FXML private Button botaoSair;
    @FXML private Button botaoFechar;

    @FXML
    private void initialize() {
        UsuarioLogado usuario = SessaoUsuario.getUsuario();
        if (usuario != null) {
            labelUsuario.setText(usuario.perfil().rotulo() + ": " + usuario.nome());
            labelBoasVindas.setText("Olá, " + usuario.nome());
        }
        carregarProvasDisponibilizadas(usuario);
    }

    private void carregarProvasDisponibilizadas(UsuarioLogado usuario) {
        listaProvasDisponibilizadas.getChildren().clear();
        if (usuario == null || usuario.perfil() != Perfil.ALUNO) {
            listaProvasDisponibilizadas.getChildren().add(
                    mensagem("Entre com sua conta de aluno para consultar as provas."));
            return;
        }

        try {
            LocalDateTime agora = LocalDateTime.now();
            var avaliacoes = DadosAplicacao.getRepositorioAvaliacoes().listarTodas();
            int quantidade = 0;
            for (Avaliacao avaliacao : avaliacoes) {
                if (!avaliacao.isDisponibilizada()
                        || !DadosAplicacao.getRepositorioTentativas()
                                .alunoMatriculadoNaAvaliacao(avaliacao.getId(), usuario.email())) {
                    continue;
                }

                LocalDateTime inicio = dataHora(avaliacao, true);
                LocalDateTime prazo = dataHora(avaliacao, false);
                String situacao = inicio != null && agora.isBefore(inicio) ? "Futura"
                        : prazo != null && !agora.isBefore(prazo) ? "Encerrada" : "Disponibilizada";

                Label titulo = new Label(avaliacao.getNome());
                titulo.getStyleClass().add("section-label");
                Label detalhes = new Label("Turma(s): " + avaliacao.getTurma()
                        + "  •  Questões: " + avaliacao.getQuestoes().size()
                        + "  •  Situação: " + situacao
                        + "  •  Início: " + formatar(inicio)
                        + "  •  Prazo: " + formatar(prazo));
                detalhes.setWrapText(true);
                VBox cartao = new VBox(6, titulo, detalhes);
                cartao.getStyleClass().add("prova-card-aluno");
                listaProvasDisponibilizadas.getChildren().add(cartao);
                quantidade++;
            }

            if (quantidade == 0) {
                listaProvasDisponibilizadas.getChildren().add(
                        mensagem("Você não tem provas disponibilizadas no momento."));
            }
        } catch (RuntimeException erro) {
            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alerta.setTitle("Erro ao carregar provas");
            alerta.setHeaderText("Não foi possível consultar as provas disponibilizadas.");
            alerta.setContentText(erro.getMessage() == null ? erro.toString() : erro.getMessage());
            alerta.showAndWait();
            listaProvasDisponibilizadas.getChildren().add(
                    mensagem("Não foi possível carregar as provas disponibilizadas."));
        }
    }

    private Label mensagem(String texto) {
        Label label = new Label(texto);
        label.setWrapText(true);
        label.getStyleClass().add("mensagem");
        return label;
    }

    private LocalDateTime dataHora(Avaliacao avaliacao, boolean inicio) {
        var data = inicio ? avaliacao.getData() : avaliacao.getPrazo();
        var hora = inicio ? avaliacao.getHoraInicio() : avaliacao.getHoraFim();
        return data == null || hora == null ? null : data.atTime(hora);
    }

    private String formatar(LocalDateTime momento) {
        return momento == null ? "Não definida" : momento.format(FORMATO_DATA_HORA);
    }

    @FXML
    private void aoClicarTurmas(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoTurmas, "minhas-turmas.fxml", "SNCTECH - Minhas Turmas");
    }

    /** A tela de avaliações lista as provas a responder e, logo abaixo, as notas das já realizadas. */
    @FXML
    private void aoClicarAvaliacoes(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoAvaliacoes, "avaliacoes-aluno.fxml", "SNCTECH - Minhas Avaliações");
    }

    @FXML
    private void aoClicarNotas(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoNotas, "minhas-notas.fxml", "SNCTECH - Minhas Notas");
    }

    @FXML
    private void aoClicarSair(ActionEvent evento) {
        NavegacaoUtil.sairDaConta(botaoSair);
    }

    @FXML
    private void aoClicarFechar(ActionEvent evento) {
        NavegacaoUtil.fecharAplicacao(botaoFechar);
    }
}
