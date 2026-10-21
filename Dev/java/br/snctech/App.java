package br.snctech;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage palco) throws Exception {
        Parent raiz = FXMLLoader.load(getClass().getResource("dashboard-professor.fxml"));

        Scene cena = new Scene(raiz, 900, 700);
        cena.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        palco.setTitle("SNCTECH - Professor");
        palco.setScene(cena);
        palco.setMinWidth(800);
        palco.setMinHeight(600);
        palco.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
