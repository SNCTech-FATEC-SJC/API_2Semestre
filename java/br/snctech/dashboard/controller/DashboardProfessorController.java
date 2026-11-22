package br.snctech.dashboard.controller;


import br.snctech.autenticacao.modelo.UsuarioLogado;
import br.snctech.banco.SessaoUsuario;
import br.snctech.banco.Conexao;
import br.snctech.infraestrutura.DadosAplicacao;
import br.snctech.util.NavegacaoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardProfessorController {

    private record Indicadores(int questoes, int provas, int turmas) { }

    @FXML private Label labelUsuario;
    @FXML private Label totalQuestoes;
    @FXML private Label totalProvas;
    @FXML private Label totalTurmas;
    @FXML private Button botaoQuestoes;
    @FXML private Button botaoAvaliacoes;
    @FXML private Button botaoTurmas;
    @FXML private Button botaoAlunos;
    @FXML private Button botaoNotas;
    @FXML private Button botaoSair;
    @FXML private Button botaoFechar;

    @FXML
    private void initialize() {
        UsuarioLogado usuario = SessaoUsuario.getUsuario();
        if (usuario != null) {
            labelUsuario.setText(usuario.perfil().rotulo() + ": " + usuario.nome());
        }
        atualizarIndicadores();
    }

    private void atualizarIndicadores() {
        try {
            Indicadores indicadores;
            if (Conexao.modoMemoria()) {
                indicadores = new Indicadores(DadosAplicacao.getRepositorioQuestoes().listarTodas().size(),
                        DadosAplicacao.getRepositorioAvaliacoes().listarTodas().size(), 0);
            } else {
                indicadores = Conexao.executar(conexao -> {
                    try (var consulta = conexao.createStatement();
                         var resultado = consulta.executeQuery(
                                 "SELECT (SELECT COUNT(*) FROM questoes), "
                                         + "(SELECT COUNT(*) FROM avaliacoes), "
                                         + "(SELECT COUNT(*) FROM turmas)")) {
                        if (!resultado.next()) throw new java.sql.SQLException("Não foi possível calcular os indicadores.");
                        return new Indicadores(resultado.getInt(1), resultado.getInt(2), resultado.getInt(3));
                    }
                });
            }
            totalQuestoes.setText(String.valueOf(indicadores.questoes()));
            totalProvas.setText(String.valueOf(indicadores.provas()));
            totalTurmas.setText(String.valueOf(indicadores.turmas()));
        } catch (RuntimeException erro) {
            totalQuestoes.setText("--");
            totalProvas.setText("--");
            totalTurmas.setText("--");
            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alerta.setTitle("Erro ao carregar painel");
            alerta.setHeaderText("Não foi possível consultar os indicadores.");
            alerta.setContentText(erro.getMessage() == null ? erro.toString() : erro.getMessage());
            alerta.showAndWait();
        }
    }

    @FXML
    private void aoClicarQuestoes(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoQuestoes, "banco-questoes.fxml", "SNCTECH - Banco de Questões");
    }

    @FXML
    private void aoClicarAvaliacoes(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoAvaliacoes, "provas-aplicadas.fxml", "SNCTECH - Provas Aplicadas");
    }

    @FXML
    private void aoClicarTurmas(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoTurmas, "turmas.fxml", "SNCTECH - Turmas");
    }

    @FXML
    private void aoClicarAlunos(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoAlunos, "alunos.fxml", "SNCTECH - Alunos");
    }

    @FXML
    private void aoClicarNotas(ActionEvent evento) {
        NavegacaoUtil.abrir(botaoNotas, "notas-turma.fxml", "SNCTECH - Notas e médias");
    }

    @FXML
    private void aoClicarSair(ActionEvent evento) {
        NavegacaoUtil.sairDaConta(botaoSair);
    }

    @FXML
    private void aoClicarFechar(ActionEvent evento) {
        NavegacaoUtil.fecharAplicacao(botaoFechar);
    }
}
