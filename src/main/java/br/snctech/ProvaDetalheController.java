package br.snctech;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

public class ProvaDetalheController {

    @FXML private Button botaoVoltar;

    @FXML
    private void aoClicarEditar(ActionEvent evento) {
        Node origem = (Node) evento.getSource();
        NavegacaoUtil.abrir(origem, "nova-prova.fxml", "SNCTECH - Nova Prova");
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoVoltar, "provas-aplicadas.fxml", "SNCTECH - Provas Aplicadas");
    }
}
