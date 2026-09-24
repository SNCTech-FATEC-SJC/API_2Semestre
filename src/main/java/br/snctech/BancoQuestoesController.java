package br.snctech;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class BancoQuestoesController {

    @FXML private Button botaoVoltar;

    @FXML
    private void aoClicarAdicionarQuestao(ActionEvent evento) {
        Node origem = (Node) evento.getSource();
        NavegacaoUtil.abrir(origem, "questoes.fxml", "SNCTECH - Questões");
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoVoltar, "dashboard-professor.fxml", "SNCTECH - Professor");
    }
}
