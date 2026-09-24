package br.snctech;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class QuestaoController {

    @FXML private Button botaoVoltar;

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoVoltar, "banco-questoes.fxml", "SNCTECH - Banco de Questões");
    }
}