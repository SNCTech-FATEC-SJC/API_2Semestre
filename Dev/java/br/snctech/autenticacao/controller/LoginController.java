package br.snctech.autenticacao.controller;

import br.snctech.autenticacao.modelo.UsuarioLogado;
import br.snctech.autenticacao.servico.AutenticacaoServico;
import br.snctech.banco.ErroBanco;
import br.snctech.banco.SessaoUsuario;
import br.snctech.util.NavegacaoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField campoEmail;
    @FXML private PasswordField campoSenha;
    @FXML private Button botaoEntrar;
    @FXML private Label labelErro;

    @FXML
    private void initialize() {
        // Chegar à tela de login sempre encerra qualquer sessão anterior.
        SessaoUsuario.encerrar();
        labelErro.setText("");
    }

    @FXML
    private void aoClicarEntrar(ActionEvent evento) {
        try {
            UsuarioLogado usuario = AutenticacaoServico.autenticar(campoEmail.getText(), campoSenha.getText());
            SessaoUsuario.iniciar(usuario);
            campoSenha.clear();
            NavegacaoUtil.abrir(botaoEntrar, usuario.perfil().arquivoDashboard(), usuario.perfil().tituloJanela());
        } catch (IllegalArgumentException | ErroBanco e) {
            labelErro.setText(e.getMessage());
            campoSenha.clear();
            campoSenha.requestFocus();
        }
    }
}
