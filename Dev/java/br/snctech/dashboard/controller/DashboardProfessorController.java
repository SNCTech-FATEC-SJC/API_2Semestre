package br.snctech.dashboard.controller;


import br.snctech.autenticacao.modelo.UsuarioLogado;
import br.snctech.banco.SessaoUsuario;
import br.snctech.util.NavegacaoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardProfessorController {

    @FXML private Label labelUsuario;
    @FXML private Button botaoQuestoes;
    @FXML private Button botaoAvaliacoes;
    @FXML private Button botaoTurmas;
    @FXML private Button botaoNotas;
    @FXML private Button botaoAlunos;
    @FXML private Button botaoSair;
    @FXML private Button botaoFechar;

    @FXML
    private void initialize() {
        UsuarioLogado usuario = SessaoUsuario.getUsuario();
        if (usuario != null) {
            labelUsuario.setText(usuario.perfil().rotulo() + ": " + usuario.nome());
        }
    }

    @FXML
    private void aoClicarQuestoes(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoQuestoes, "banco-questoes.fxml", "SNCTECH - Banco de Questões");
    }

    @FXML
    private void aoClicarAvaliacoes(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoAvaliacoes, "provas-aplicadas.fxml", "SNCTECH - Provas Aplicadas");
    }

    @FXML
    private void aoClicarTurmas(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoTurmas, "turmas.fxml", "SNCTECH - Turmas");
    }

    @FXML
    private void aoClicarNotas(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoNotas, "notas-turma.fxml", "SNCTECH - Notas e médias");
    }

    @FXML
    private void aoClicarAlunos(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoAlunos, "alunos.fxml", "SNCTECH - Alunos");
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
