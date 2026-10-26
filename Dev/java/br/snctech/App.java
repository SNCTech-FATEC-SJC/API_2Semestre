package br.snctech;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import br.snctech.banco.Conexao;
import br.snctech.banco.ErroBanco;
import br.snctech.banco.QuestaoDAO;
import br.snctech.banco.SessaoUsuario;
import java.util.ArrayList;
import javafx.scene.control.Alert;

public class App extends Application {

    @Override
    public void start(Stage palco) throws Exception {
        if (!Conexao.modoMemoria()) {
            String problema = Conexao.verificarEsquema();
            if (problema != null) {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Erro na configuração do banco");
                alerta.setHeaderText("Não foi possível iniciar com o PostgreSQL");
                alerta.setContentText(problema);
                alerta.showAndWait();
                Conexao.fechar();
                return;
            }
        }
        if (!Conexao.modoMemoria()) {
            // Carrega no catálogo compartilhado as questões que já pertencem ao professor do banco.
            int professorId = SessaoUsuario.getProfessorId();
            var questoesDoBanco = Conexao.executar(c -> QuestaoDAO.carregarDoProfessor(c, professorId));
            BancoQuestoes.getQuestoes().setAll(questoesDoBanco.values());
        }

        Parent raiz = FXMLLoader.load(getClass().getResource("dashboard-professor.fxml"));

        Scene cena = new Scene(raiz, 900, 700);
        cena.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        palco.setTitle("SNCTECH - Professor");
        palco.setScene(cena);
        palco.setMinWidth(800);
        palco.setMinHeight(600);
        palco.show();
        palco.setOnCloseRequest(event -> Conexao.fechar());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
