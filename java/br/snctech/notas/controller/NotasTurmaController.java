package br.snctech.notas.controller;

import br.snctech.banco.SessaoUsuario;
import br.snctech.notas.DadosNotas;
import br.snctech.notas.model.AvaliacaoDaTurma;
import br.snctech.notas.model.LinhaDoQuadro;
import br.snctech.notas.model.QuadroDaTurma;
import br.snctech.notas.model.TurmaResumo;
import br.snctech.util.NavegacaoUtil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.function.Function;

public class NotasTurmaController {

    private static final int MAXIMO_DE_PENDENCIAS_NA_TELA = 6;

    @FXML private ComboBox<TurmaResumo> comboTurma;
    @FXML private TableView<LinhaDoQuadro> tabela;
    @FXML private Label mensagem;
    @FXML private Button botaoLiberar;
    @FXML private Button botaoAtualizar;
    @FXML private Button botaoVoltar;

    @FXML
    private void initialize() {
        tabela.setPlaceholder(new Label("Selecione uma turma para ver as notas."));
        botaoLiberar.setDisable(true);
        String problema = DadosNotas.verificarTabela();
        if (problema != null) {
            bloquear(problema);
            return;
        }
        try {
            List<TurmaResumo> turmas = DadosNotas.servico().turmasDoProfessor(SessaoUsuario.getProfessorId());
            comboTurma.setItems(FXCollections.observableArrayList(turmas));
            comboTurma.valueProperty().addListener((obs, antiga, nova) -> carregar(nova));
            if (turmas.isEmpty()) {
                bloquear("Você ainda não tem turmas cadastradas.");
            } else {
                comboTurma.getSelectionModel().selectFirst();
            }
        } catch (RuntimeException erro) {
            bloquear("Não foi possível carregar as turmas. Detalhe: " + erro.getMessage());
        }
    }

    @FXML
    private void aoClicarAtualizar(ActionEvent evento) {
        carregar(comboTurma.getValue());
    }

    @FXML
    private void aoClicarLiberar(ActionEvent evento) {
        TurmaResumo turma = comboTurma.getValue();
        if (turma == null) {
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Concluir turma e liberar médias");
        confirmacao.setHeaderText("Concluir a turma \"" + turma.nome() + "\" e liberar as médias?");
        confirmacao.setContentText("Os alunos passam a ver a média final quando todas as avaliações estiverem corrigidas. "
                + "A conclusão também verifica se há alunos elegíveis para recuperação.");
        if (confirmacao.showAndWait().filter(botao -> botao == ButtonType.OK).isEmpty()) {
            return;
        }
        try {
            mostrar(DadosNotas.servico().liberar(turma));
        } catch (IllegalStateException erro) {
            avisar(erro.getMessage());
        } catch (RuntimeException erro) {
            avisar("Não foi possível liberar as médias. Detalhe: " + erro.getMessage());
        }
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoVoltar, "dashboard-professor.fxml", "SNCTECH - Professor");
    }

    private void carregar(TurmaResumo turma) {
        if (turma == null) {
            return;
        }
        try {
            mostrar(DadosNotas.servico().montarQuadro(turma));
        } catch (RuntimeException erro) {
            tabela.getItems().clear();
            botaoLiberar.setDisable(true);
            mensagem.setText("Não foi possível carregar as notas da turma. Detalhe: " + erro.getMessage());
        }
    }

    private void mostrar(QuadroDaTurma quadro) {
        tabela.getColumns().clear();
        tabela.getColumns().add(coluna("Aluno", 160, linha -> linha.aluno().nome()));
        for (AvaliacaoDaTurma avaliacao : quadro.avaliacoes()) {
            String titulo = avaliacao.titulo() + "\npeso " + avaliacao.textoDoPeso();
            tabela.getColumns().add(coluna(titulo, Math.max(275, avaliacao.titulo().length() * 9.0),
                    linha -> linha.textoDaNota(avaliacao.id())));
        }
        tabela.getColumns().add(coluna("Média final", 100, linha -> linha.media().textoDaMedia()));
        tabela.getColumns().add(coluna("Situação", 220, LinhaDoQuadro::textoDaSituacao));
        tabela.getColumns().add(coluna("Para o aluno", 110, LinhaDoQuadro::textoDaLiberacao));
        tabela.setItems(FXCollections.observableArrayList(quadro.linhas()));
        tabela.setPlaceholder(new Label("Nenhum aluno matriculado nesta turma."));

        botaoLiberar.setDisable(!quadro.podeLiberar() || quadro.todasLiberadas());
        mensagem.setText(textoDoEstado(quadro));
    }

    private String textoDoEstado(QuadroDaTurma quadro) {
        if (quadro.linhas().isEmpty()) {
            return "Nenhum aluno matriculado nesta turma.";
        }
        if (quadro.avaliacoes().isEmpty()) {
            return "Esta turma ainda não tem avaliações liberadas.";
        }
        List<String> pendencias = quadro.pendencias();
        if (!pendencias.isEmpty()) {
            StringBuilder texto = new StringBuilder("A média final só sai quando todas as avaliações estiverem encerradas e corrigidas. Pendências:");
            pendencias.stream().limit(MAXIMO_DE_PENDENCIAS_NA_TELA).forEach(p -> texto.append("\n• ").append(p));
            if (pendencias.size() > MAXIMO_DE_PENDENCIAS_NA_TELA) {
                texto.append("\n• e mais ").append(pendencias.size() - MAXIMO_DE_PENDENCIAS_NA_TELA).append(" pendência(s).");
            }
            return texto.toString();
        }
        if (quadro.todasLiberadas()) {
            return "Médias liberadas: os alunos já veem a média final e a situação.";
        }
        return "Todas as médias foram calculadas. Conclua a turma para os alunos verem os resultados.";
    }

    private TableColumn<LinhaDoQuadro, String> coluna(String titulo, double largura,
                                                       Function<LinhaDoQuadro, String> valor) {
        TableColumn<LinhaDoQuadro, String> coluna = new TableColumn<>(titulo);
        coluna.setPrefWidth(largura);
        coluna.setSortable(false);
        coluna.setCellValueFactory(dados -> new ReadOnlyStringWrapper(valor.apply(dados.getValue())));
        return coluna;
    }

    private void bloquear(String texto) {
        mensagem.setText(texto);
        comboTurma.setDisable(true);
        botaoAtualizar.setDisable(true);
        botaoLiberar.setDisable(true);
    }

    private void avisar(String texto) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Médias");
        alerta.setHeaderText(null);
        alerta.setContentText(texto);
        alerta.showAndWait();
    }
}
