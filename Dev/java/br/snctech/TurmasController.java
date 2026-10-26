package br.snctech;

import java.sql.Connection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class TurmasController {

    @FXML private TextField txtNomeTurma;
    @FXML private Button botaoCadastrar;
    @FXML private Button botaoVoltar;

    /**
     * Método acionado ao clicar no botão de cadastrar turma
     */
    @FXML
    private void aoClicarCadastrar(ActionEvent evento) {
        String nomeTurma = txtNomeTurma.getText().trim();

        // Validação básica se o campo está vazio
        if (nomeTurma.isEmpty()) {
            exibirAlerta(AlertType.WARNING, "Atenção", "O nome da turma não pode estar vazio!");
            return;
        }

        // TODO: Substitua pelo ID real do professor logado no seu sistema (ex: Sessao.getProfessorId())
        int professorId = 1; // Exemplo fixo provisório

        // Obtém a conexão com o banco de dados (verifique se o nome da sua classe de conexão é Conexao)
        try (Connection conexao = Conexao.conectar()) { 
            
            // Chama o método do DAO para cadastrar a nova turma e desativar a anterior
            TurmaDAO.cadastrarTurma(conexao, professorId, nomeTurma);

            exibirAlerta(AlertType.INFORMATION, "Sucesso", "Turma cadastrada com sucesso! A anterior foi encerrada.");
            
            // Limpa o campo após o cadastro
            txtNomeTurma.clear();

        } catch (Exception e) {
            exibirAlerta(AlertType.ERROR, "Erro", "Erro ao conectar ou salvar no banco: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoVoltar, "dashboard-professor.fxml", "SNCTECH - Professor");
    }

    /**
     * Método auxiliar para exibir alertas visuais para o usuário no JavaFX
     */
    private void exibirAlerta(AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}