package br.snctech;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class TurmasController {

    @FXML private Button botaoVoltar;

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoVoltar, "dashboard-professor.fxml", "SNCTECH - Professor");
    }
}
