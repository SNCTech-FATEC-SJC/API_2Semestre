package br.snctech.dashboard.controller;

import br.snctech.autenticacao.modelo.UsuarioLogado;
import br.snctech.banco.SessaoUsuario;
import br.snctech.util.NavegacaoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardAlunoController {

    @FXML private Label labelUsuario;
    @FXML private Label labelBoasVindas;
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
        NavegacaoUtil.abrir(botaoNotas, "avaliacoes-aluno.fxml", "SNCTECH - Minhas Avaliações e Notas");
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
