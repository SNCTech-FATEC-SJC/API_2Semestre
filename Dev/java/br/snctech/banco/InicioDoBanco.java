package br.snctech.banco;

import br.snctech.banco.PreparadorBanco.Falha;
import br.snctech.banco.PreparadorBanco.ProblemaNoBanco;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public final class InicioDoBanco {

    private InicioDoBanco() {
    }

    public static boolean preparar() {
        if (Conexao.modoMemoria()) return true;
        String aviso = null;
        while (true) {
            try {
                PreparadorBanco.Resultado resultado = PreparadorBanco.padrao().preparar();
                if (!resultado.scriptsAplicados().isEmpty()) {
                    System.out.println("Banco preparado" + (resultado.bancoCriado() ? " (banco criado agora)" : "")
                            + ". Scripts aplicados: " + String.join(", ", resultado.scriptsAplicados()));
                }
                return true;
            } catch (ProblemaNoBanco problema) {
                if (problema.falha() != Falha.SENHA) {
                    mostrarErro(problema.getMessage());
                    return false;
                }
                Optional<String[]> acesso = pedirAcesso(aviso == null ? problema.getMessage() : aviso);
                if (acesso.isEmpty()) return false;
                try {
                    File salvo = Conexao.salvarAcesso(acesso.get()[0], acesso.get()[1]);
                    System.out.println("Acesso ao banco salvo em " + salvo.getAbsolutePath());
                } catch (IOException e) {
                    mostrarErro("Não foi possível salvar o arquivo banco.properties: " + e.getMessage());
                    return false;
                }
                aviso = "A senha não funcionou. Tente de novo.";
            }
        }
    }

    private static Optional<String[]> pedirAcesso(String motivo) {
        Dialog<String[]> janela = new Dialog<>();
        janela.setTitle("SNCTECH - Acesso ao banco de dados");
        janela.setHeaderText("Informe a senha do PostgreSQL deste computador");
        ButtonType entrar = new ButtonType("Conectar", ButtonBar.ButtonData.OK_DONE);
        janela.getDialogPane().getButtonTypes().addAll(entrar, ButtonType.CANCEL);

        TextField campoUsuario = new TextField(Conexao.usuario());
        PasswordField campoSenha = new PasswordField();
        Label explicacao = new Label(motivo + "\nÉ a senha escolhida ao instalar o PostgreSQL. Ela fica salva só "
                + "neste computador, no arquivo banco.properties, que não vai para o Git.");
        explicacao.setWrapText(true);
        explicacao.setMaxWidth(420);

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.add(explicacao, 0, 0, 2, 1);
        grade.add(new Label("Usuário:"), 0, 1);
        grade.add(campoUsuario, 1, 1);
        grade.add(new Label("Senha:"), 0, 2);
        grade.add(campoSenha, 1, 2);
        janela.getDialogPane().setContent(grade);
        Platform.runLater(campoSenha::requestFocus);

        janela.setResultConverter(botao -> botao == entrar
                ? new String[]{campoUsuario.getText().trim(), campoSenha.getText()}
                : null);
        return janela.showAndWait();
    }

    private static void mostrarErro(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro na configuração do banco");
        alerta.setHeaderText("Não foi possível preparar o PostgreSQL");
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
