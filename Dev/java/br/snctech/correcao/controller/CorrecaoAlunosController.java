package br.snctech.correcao.controller;

import br.snctech.NavegacaoUtil;
import br.snctech.correcao.DadosCorrecao;
import br.snctech.correcao.controller.PecasVisuais.Cor;
import br.snctech.correcao.service.ResumoCorrecaoService.LinhaAluno;
import br.snctech.correcao.service.ResumoCorrecaoService.ResumoAvaliacao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Tela 6 - Correção da prova: lista os alunos da avaliação com a situação de cada um.
 *
 * <p>Como abrir a partir de outra tela:
 * <pre>
 *   DadosCorrecao.prepararCorrecao(idAvaliacao, nome, turma, n -> NavegacaoUtil.abrir(n, "prova-detalhe.fxml", "..."));
 *   NavegacaoUtil.abrir(origem, "correcao-alunos.fxml", "SNCTECH - Correção da prova");
 * </pre>
 */
public class CorrecaoAlunosController {

    @FXML private Label tituloAvaliacao;
    @FXML private Label subtitulo;
    @FXML private Label rotuloPercentual;
    @FXML private ProgressBar barraProgresso;
    @FXML private GridPane tabela;
    @FXML private Button botaoVoltar;

    @FXML
    private void initialize() {
        var ctx = DadosCorrecao.contexto();
        tituloAvaliacao.setText(ctx.nomeAvaliacao());
        subtitulo.setText(ctx.nomeAvaliacao() + " · " + ctx.nomeTurma());
        botaoVoltar.setVisible(ctx.aoVoltar() != null);
        botaoVoltar.setManaged(ctx.aoVoltar() != null);

        ResumoAvaliacao resumo = DadosCorrecao.resumo()
                .resumir(ctx.idAvaliacao(), DadosCorrecao.prazoEncerrado(ctx.idAvaliacao()));
        barraProgresso.setProgress(resumo.percentualCorrigido() / 100.0);
        rotuloPercentual.setText(resumo.percentualCorrigido() + "%");

        montarTabela(resumo);
    }

    private void montarTabela(ResumoAvaliacao resumo) {
        tabela.getChildren().clear();
        tabela.getColumnConstraints().clear();
        double[] larguras = {32, 20, 18, 12, 18};
        for (double l : larguras) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(l);
            c.setHgrow(Priority.ALWAYS);
            tabela.getColumnConstraints().add(c);
        }

        String[] titulos = {"ALUNO", "SITUAÇÃO", "DISCURSIVAS PENDENTES", "NOTA", ""};
        for (int i = 0; i < titulos.length; i++) {
            Label l = new Label(titulos[i]);
            l.getStyleClass().add("corr-cabecalho-tabela");
            tabela.add(l, i, 0);
        }

        if (resumo.alunos().isEmpty()) {
            tabela.add(new Label("Nenhum aluno encontrado para esta avaliação."), 0, 1, 5, 1);
            return;
        }
        int linha = 1;
        for (LinhaAluno aluno : resumo.alunos()) {
            tabela.add(new Label(aluno.nome()), 0, linha);
            tabela.add(seloDaSituacao(aluno), 1, linha);
            tabela.add(new Label(aluno.discursivasPendentes() > 0 ? String.valueOf(aluno.discursivasPendentes()) : "–"), 2, linha);
            tabela.add(new Label(PecasVisuais.formatar(aluno.nota())), 3, linha);
            tabela.add(botaoAcao(aluno), 4, linha);
            GridPane.setHalignment(tabela.getChildren().get(tabela.getChildren().size() - 1), HPos.RIGHT);
            for (Node n : tabela.getChildren()) {
                if (GridPane.getRowIndex(n) != null && GridPane.getRowIndex(n) == linha) {
                    GridPane.setValignment(n, VPos.CENTER);
                }
            }
            linha++;
        }
        tabela.setVgap(10);
    }

    private static Label seloDaSituacao(LinhaAluno aluno) {
        return switch (aluno.situacao()) {
            case CORRIGIDA -> PecasVisuais.selo("Corrigida", Cor.VERDE);
            case PENDENTE -> PecasVisuais.selo("Pendente", Cor.LARANJA);
            case NAO_RESPONDEU -> PecasVisuais.selo("Não respondeu", Cor.CINZA);
        };
    }

    private Node botaoAcao(LinhaAluno aluno) {
        if (aluno.idAvaliacaoRespondida() == null) {
            return new Label("–");
        }
        Button botao = new Button(aluno.discursivasPendentes() > 0 ? "Corrigir" : "Revisar");
        botao.getStyleClass().add("primary-button");
        botao.setOnAction(e -> {
            DadosCorrecao.selecionarAluno(aluno.idAluno());
            NavegacaoUtil.abrir(botao, "correcao-manual.fxml", "SNCTECH - Correção manual");
        });
        return botao;
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        var aoVoltar = DadosCorrecao.contexto().aoVoltar();
        if (aoVoltar != null) {
            aoVoltar.accept((Node) evento.getSource());
        }
    }
}
