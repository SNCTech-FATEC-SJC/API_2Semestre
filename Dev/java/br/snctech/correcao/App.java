package br.snctech.correcao;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Aplicação de demonstração das telas de Correção rodando SOZINHAS (antes da unificação).
 * Abre a Tela 6; de lá o professor entra na Tela 7.
 *
 * <p>Na versão unificada esta classe deixa de ser usada: o {@code br.snctech.App} do grupo
 * abre o painel e as telas de correção são abertas por {@code NavegacaoUtil}.
 * Os dados são de demonstração, em memória.
 */
public class App extends Application {

    @Override
    public void start(Stage palco) throws Exception {
        DadosCorrecao.usarMemoria();

        Parent raiz = FXMLLoader.load(getClass().getResource("/br/snctech/correcao-alunos.fxml"));
        Scene cena = new Scene(raiz, 1100, 720);
        cena.getStylesheets().add(getClass().getResource("/br/snctech/styles.css").toExternalForm());

        palco.setTitle("SNCTECH - Correção da prova");
        palco.setScene(cena);
        palco.setMinWidth(900);
        palco.setMinHeight(600);
        palco.show();
    }
}
