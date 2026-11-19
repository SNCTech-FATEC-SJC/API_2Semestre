package br.snctech.alunos.controller;

import br.snctech.alunos.repositorio.AlunoDAO;
import br.snctech.autenticacao.modelo.UsuarioLogado;
import br.snctech.banco.Conexao;
import br.snctech.banco.ErroBanco;
import br.snctech.banco.SessaoUsuario;
import br.snctech.turmas.modelo.Turma;
import br.snctech.util.NavegacaoUtil;
import br.snctech.util.TabelaUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

/** Tela do aluno: lista das turmas em que ele está (ativas e concluídas). Somente leitura. */
public class MinhasTurmasController {

    @FXML private Button botaoVoltar;
    @FXML private TableView<Turma> tabela;
    @FXML private Label labelResumo;
    @FXML private Label labelMensagem;

    @FXML
    private void initialize() {
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Você ainda não está matriculado em nenhuma turma."));

        TableColumn<Turma, String> colunaSituacao = TabelaUtil.<Turma>coluna("Situação", 110, Turma::situacao);
        TabelaUtil.estilizarSituacao(colunaSituacao);

        tabela.getColumns().add(TabelaUtil.<Turma>coluna("Turma", 260, Turma::nome));
        tabela.getColumns().add(TabelaUtil.<Turma>coluna("Professor responsável", 240, Turma::professorNome));
        tabela.getColumns().add(colunaSituacao);
        tabela.getColumns().add(TabelaUtil.<Turma>coluna("Alunos", 80, t -> String.valueOf(t.totalAlunos())));
        tabela.getColumns().add(TabelaUtil.<Turma>coluna("Criada em", 150, t -> TabelaUtil.data(t.criadoEm())));

        carregar();
    }

    private void carregar() {
        UsuarioLogado usuario = SessaoUsuario.getUsuario();
        if (usuario == null) {
            labelMensagem.getStyleClass().add("erro");
            labelMensagem.setText("Sessão expirada. Faça login novamente.");
            return;
        }
        try {
            List<Turma> turmas = Conexao.executar(c -> AlunoDAO.listarTurmasDoAluno(c, usuario.id()));
            tabela.setItems(FXCollections.observableArrayList(turmas));
            long ativas = turmas.stream().filter(Turma::ativa).count();
            labelResumo.setText(turmas.size() + " turma(s): " + ativas + " ativa(s) e "
                    + (turmas.size() - ativas) + " concluída(s)");
        } catch (ErroBanco e) {
            labelMensagem.getStyleClass().add("erro");
            labelMensagem.setText(e.getMessage());
        }
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrirInicio(botaoVoltar);
    }
}
