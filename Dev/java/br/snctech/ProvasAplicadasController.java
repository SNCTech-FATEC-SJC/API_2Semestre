package br.snctech;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class ProvasAplicadasController {

    @FXML private Button botaoVoltar;

    @FXML
    private void aoClicarNovaProva(ActionEvent evento) {
        Node origem = (Node) evento.getSource();
        NavegacaoUtil.abrir(origem, "nova-prova.fxml", "SNCTECH - Nova Prova");
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoVoltar, "dashboard-professor.fxml", "SNCTECH - Professor");
    }

    @FXML
    private void aoClicarProva(javafx.scene.input.MouseEvent evento) {
        Node origem = (Node) evento.getSource();
        NavegacaoUtil.abrir(origem, "prova-detalhe.fxml", "SNCTECH - Prova");
    }
}
