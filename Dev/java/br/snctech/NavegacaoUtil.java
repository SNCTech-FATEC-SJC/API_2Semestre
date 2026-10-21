package br.snctech;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Troca o conteudo da janela atual por outra tela.
 * Use a partir de qualquer controller: NavegacaoUtil.abrir(botaoEntrar, "turmas.fxml", "SNCTECH - Turmas");
 */
public final class NavegacaoUtil {

    private NavegacaoUtil() {
    }

    public static void abrir(Node origem, String arquivoFxml, String titulo) {
        try {
            Parent raiz = FXMLLoader.load(NavegacaoUtil.class.getResource(arquivoFxml));

            Stage palco = (Stage) origem.getScene().getWindow();
            Scene cena = new Scene(raiz, palco.getWidth(), palco.getHeight());
            cena.getStylesheets().add(
                    NavegacaoUtil.class.getResource("styles.css").toExternalForm());

            palco.setTitle(titulo);
            palco.setScene(cena);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível abrir a tela: " + arquivoFxml, e);
        }
    }
}
