package br.snctech.util;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

/**
 * Diálogos padronizados do SNCTECH (alertas, confirmações e estilo dos formulários em janela).
 */
public final class DialogoUtil {

    public static final ButtonType SALVAR = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
    public static final ButtonType CANCELAR = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
    public static final ButtonType FECHAR = new ButtonType("Fechar", ButtonBar.ButtonData.CANCEL_CLOSE);

    private static final ButtonType SIM = new ButtonType("Sim", ButtonBar.ButtonData.OK_DONE);
    private static final ButtonType NAO = new ButtonType("Não", ButtonBar.ButtonData.CANCEL_CLOSE);

    private DialogoUtil() {
    }

    /**
     * Aplica o styles.css, define a janela dona e estiliza os botões.
     * Deve ser chamado DEPOIS de adicionar os ButtonTypes ao DialogPane.
     */
    public static void estilizar(Dialog<?> dialogo, Node origem) {
        if (origem != null && origem.getScene() != null) {
            dialogo.initOwner(origem.getScene().getWindow());
        }
        DialogPane painel = dialogo.getDialogPane();
        painel.getStylesheets().add(DialogoUtil.class.getResource("/br/snctech/styles.css").toExternalForm());
        for (ButtonType tipo : painel.getButtonTypes()) {
            Node botao = painel.lookupButton(tipo);
            if (botao != null) {
                botao.getStyleClass().add(tipo.getButtonData().isCancelButton() ? "botao-secundario" : "botao-primario");
            }
        }
    }

    public static void informar(Node origem, String mensagem) {
        mostrar(origem, Alert.AlertType.INFORMATION, "SNCTECH", mensagem);
    }

    public static void erro(Node origem, String mensagem) {
        mostrar(origem, Alert.AlertType.ERROR, "SNCTECH - Erro", mensagem);
    }

    public static boolean confirmar(Node origem, String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensagem, SIM, NAO);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        estilizar(alerta, origem);
        return alerta.showAndWait().filter(botao -> botao == SIM).isPresent();
    }

    private static void mostrar(Node origem, Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo, mensagem, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        estilizar(alerta, origem);
        alerta.showAndWait();
    }
}
