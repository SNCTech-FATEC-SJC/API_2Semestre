package br.snctech.turmas.repositorio;

import br.snctech.notas.service.CalculoMediaService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Reutiliza turmas e matrículas existentes para registrar os alunos de recuperação. */
final class RecuperacaoTurmaDAO {

    private static final String PREFIXO_RECUPERACAO = "Recuperação turma #";

    private RecuperacaoTurmaDAO() { }

    static void criarSeNecessario(Connection c, int idTurma, String nomeTurma) throws SQLException {
        if (nomeTurma == null || nomeTurma.toLowerCase(Locale.ROOT).startsWith(PREFIXO_RECUPERACAO.toLowerCase(Locale.ROOT))) {
            return;
        }

        int professorId;
        try (PreparedStatement ps = c.prepareStatement("SELECT professor_id FROM turmas WHERE id = ?")) {
            ps.setInt(1, idTurma);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Turma não encontrada ao verificar recuperação: " + idTurma);
                }
                professorId = rs.getInt("professor_id");
            }
        }

        List<Integer> elegiveis = alunosElegiveis(c, idTurma);
        List<Integer> novosMembros = new ArrayList<>();
        for (int idAluno : elegiveis) {
            if (!alunoJaEstaEmRecuperacao(c, idAluno)) {
                novosMembros.add(idAluno);
            }
        }
        if (novosMembros.isEmpty()) {
            return;
        }

        String prefixo = PREFIXO_RECUPERACAO + idTurma;
        String nomeRecuperacao = prefixo + " - " + nomeTurma;
        if (nomeRecuperacao.length() > 100) {
            nomeRecuperacao = nomeRecuperacao.substring(0, 100);
        }
        int idRecuperacao;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id FROM turmas WHERE professor_id = ? AND nome LIKE ? ORDER BY id LIMIT 1")) {
            ps.setInt(1, professorId);
            ps.setString(2, prefixo + " - %");
            try (ResultSet rs = ps.executeQuery()) {
                idRecuperacao = rs.next() ? rs.getInt("id") : -1;
            }
        }
        if (idRecuperacao < 0) {
            try (PreparedStatement criar = c.prepareStatement(
                    "INSERT INTO turmas (nome, professor_id, ativa) VALUES (?, ?, FALSE) RETURNING id")) {
                criar.setString(1, nomeRecuperacao);
                criar.setInt(2, professorId);
                try (ResultSet novaTurma = criar.executeQuery()) {
                    if (!novaTurma.next()) {
                        throw new SQLException("Não foi possível criar a turma de recuperação.");
                    }
                    idRecuperacao = novaTurma.getInt(1);
                }
            }
        }

        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO matriculas (turma_id, aluno_id) "
                        + "SELECT ?, ? WHERE NOT EXISTS "
                        + "(SELECT 1 FROM matriculas WHERE turma_id = ? AND aluno_id = ?)")) {
            for (int idAluno : novosMembros) {
                ps.setInt(1, idRecuperacao);
                ps.setInt(2, idAluno);
                ps.setInt(3, idRecuperacao);
                ps.setInt(4, idAluno);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static List<Integer> alunosElegiveis(Connection c, int idTurma) throws SQLException {
        String somaPonderada = "SUM(CASE WHEN t.status::text IN ('enviada', 'nao_realizada_prazo')"
                + " AND COALESCE(t.corrigida_completamente, false)"
                + " THEN COALESCE(t.nota_total, 0) ELSE 0 END) / NULLIF(SUM(a.peso), 0)";
        String sql = "WITH alunos AS ("
                + " SELECT DISTINCT aluno_id FROM matriculas WHERE turma_id = ?"
                + "), provas AS ("
                + " SELECT DISTINCT a.id, a.peso, a.prazo_fim FROM avaliacao_turmas avt"
                + " JOIN avaliacoes a ON a.id = avt.avaliacao_id"
                + " WHERE avt.turma_id = ? AND COALESCE(a.liberada, false)"
                + ") SELECT alunos.aluno_id, " + somaPonderada + " AS media"
                + " FROM alunos CROSS JOIN provas a"
                + " LEFT JOIN tentativas_avaliacao t"
                + " ON t.aluno_id = alunos.aluno_id AND t.avaliacao_id = a.id"
                + " GROUP BY alunos.aluno_id"
                + " HAVING BOOL_AND(a.prazo_fim < CURRENT_TIMESTAMP AND ("
                + " t.id IS NULL OR (t.status::text IN ('enviada', 'nao_realizada_prazo')"
                + " AND COALESCE(t.corrigida_completamente, false))))";
        List<Integer> alunos = new ArrayList<>();
        CalculoMediaService calculo = new CalculoMediaService();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idTurma);
            ps.setInt(2, idTurma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (calculo.estaNaFaixaDeRecuperacao(rs.getBigDecimal("media"))) {
                        alunos.add(rs.getInt("aluno_id"));
                    }
                }
            }
        }
        return alunos;
    }

    private static boolean alunoJaEstaEmRecuperacao(Connection c, int idAluno) throws SQLException {
        String sql = "SELECT EXISTS (SELECT 1 FROM matriculas m JOIN turmas t ON t.id = m.turma_id "
                + "WHERE m.aluno_id = ? AND lower(t.nome) LIKE lower('Recuperação turma #%'))";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idAluno);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }
}
