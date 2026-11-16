package br.snctech.autenticacao.controller;


import br.snctech.util.NavegacaoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class LoginController {

	@FXML private Button botaoEntrar;

	@FXML
	private void aoClicarEntrar(ActionEvent evento) {
		NavegacaoUtil.abrir(botaoEntrar, "dashboard-professor.fxml", "SNCTECH - Professor");
	}
}
