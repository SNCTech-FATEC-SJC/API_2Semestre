package br.snctech;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

public class NovaProvaController {

    @FXML private Button botaoVoltar;

    @FXML
    private void aoClicarSalvar(ActionEvent evento) {
        Node origem = (Node) evento.getSource();
        NavegacaoUtil.abrir(origem, "provas-aplicadas.fxml", "SNCTECH - Provas Aplicadas");
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoVoltar, "provas-aplicadas.fxml", "SNCTECH - Provas Aplicadas");
    }
}
