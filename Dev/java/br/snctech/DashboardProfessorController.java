package br.snctech;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DashboardProfessorController {

    @FXML private Button botaoQuestoes;
    @FXML private Button botaoAvaliacoes;
    @FXML private Button botaoCorrecao;
    @FXML private Button botaoTurmas;

    @FXML
    private void aoClicarQuestoes(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoQuestoes, "banco-questoes.fxml", "SNCTECH - Banco de Questões");
    }

    @FXML
    private void aoClicarAvaliacoes(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoAvaliacoes, "provas-aplicadas.fxml", "SNCTECH - Provas Aplicadas");
    }

    @FXML
    private void aoClicarCorrecao(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoCorrecao, "provas-aplicadas.fxml", "SNCTECH - Provas Aplicadas");
    }

    @FXML
    private void aoClicarTurmas(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoTurmas, "turmas.fxml", "SNCTECH - Turmas");
    }
}
