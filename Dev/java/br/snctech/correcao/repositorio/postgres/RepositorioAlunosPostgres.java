package br.snctech.correcao.repositorio.postgres;

import br.snctech.banco.Conexao;
import br.snctech.correcao.model.AlunoResumo;
import br.snctech.correcao.repositorio.RepositorioAlunos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Alunos de uma avaliação, no PostgreSQL: os matriculados na turma da avaliação (tabela matriculas)
 * mais os que já abriram a prova (tentativas_avaliacao). Assim a lista também funciona enquanto
 * as matrículas não estiverem cadastradas, e quem não respondeu aparece quando está matriculado.
 */
public class RepositorioAlunosPostgres implements RepositorioAlunos {

    @Override
    public List<AlunoResumo> buscarPorAvaliacao(Integer idAvaliacao) {
        return Conexao.executar(c -> {
            String sql = "SELECT u.id, u.nome FROM usuarios u WHERE u.id IN ("
                    + "SELECT m.aluno_id FROM matriculas m JOIN avaliacoes a ON a.turma_id = m.turma_id WHERE a.id = ? "
                    + "UNION SELECT t.aluno_id FROM tentativas_avaliacao t WHERE t.avaliacao_id = ?) "
                    + "ORDER BY u.nome";
            List<AlunoResumo> alunos = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, idAvaliacao);
                ps.setInt(2, idAvaliacao);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        alunos.add(new AlunoResumo(rs.getInt("id"), rs.getString("nome")));
                    }
                }
            }
            return alunos;
        });
    }
}
