package br.snctech.correcao;

import javafx.application.Application;

/**
 * Ponto de partida. Existe separado da classe App (que estende Application) porque,
 * com JavaFX no classpath do Maven, iniciar direto uma classe que estende Application
 * costuma dar "JavaFX runtime components are missing".
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
