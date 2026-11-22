package br.snctech.notas.repositorio;

import br.snctech.banco.Conexao;
import br.snctech.notas.model.AlunoDaTurma;
import br.snctech.notas.model.AvaliacaoDaTurma;
import br.snctech.notas.model.TentativaRegistrada;
import br.snctech.notas.model.TurmaResumo;
import br.snctech.turmas.repositorio.TurmaDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RepositorioMediasPostgres implements RepositorioMedias {

    @Override
    public List<TurmaResumo> turmasDoProfessor(int idProfessor) {
        return Conexao.executar(c -> turmasDoProfessor(c, idProfessor));
    }

    @Override
    public List<TurmaResumo> turmasDoAluno(String email) {
        return Conexao.executar(c -> turmasDoAluno(c, email));
    }

    @Override
    public List<AvaliacaoDaTurma> avaliacoesDaTurma(int idTurma) {
        return Conexao.executar(c -> avaliacoesDaTurma(c, idTurma));
    }

    @Override
    public List<AlunoDaTurma> alunosDaTurma(int idTurma) {
        return Conexao.executar(c -> alunosDaTurma(c, idTurma));
    }

    @Override
    public List<TentativaRegistrada> tentativasDaTurma(int idTurma) {
        return Conexao.executar(c -> tentativasDaTurma(c, idTurma));
    }

    @Override
    public boolean turmaConcluida(int idTurma) {
        return Conexao.executar(c -> turmaConcluida(c, idTurma));
    }

    @Override
    public void liberar(int idTurma) {
        Conexao.emTransacao(c -> TurmaDAO.concluir(c, idTurma));
    }

    public static List<TurmaResumo> turmasDoProfessor(Connection c, int idProfessor) throws SQLException {
        String sql = "SELECT id, nome FROM turmas WHERE professor_id = ? ORDER BY nome, id";
        List<TurmaResumo> turmas = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProfessor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    turmas.add(new TurmaResumo(rs.getInt("id"), rs.getString("nome")));
                }
            }
        }
        return turmas;
    }

    public static List<TurmaResumo> turmasDoAluno(Connection c, String email) throws SQLException {
        String sql = "SELECT DISTINCT t.id, t.nome FROM turmas t "
                + "JOIN matriculas m ON m.turma_id = t.id "
                + "JOIN usuarios u ON u.id = m.aluno_id "
                + "WHERE lower(u.email) = lower(?) ORDER BY t.nome, t.id";
        List<TurmaResumo> turmas = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email == null ? "" : email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    turmas.add(new TurmaResumo(rs.getInt("id"), rs.getString("nome")));
                }
            }
        }
        return turmas;
    }

    public static List<AvaliacaoDaTurma> avaliacoesDaTurma(Connection c, int idTurma) throws SQLException {
        String sql = "SELECT a.id, a.titulo, a.peso, a.prazo_fim FROM avaliacao_turmas avt "
                + "JOIN avaliacoes a ON a.id = avt.avaliacao_id "
                + "WHERE avt.turma_id = ? AND COALESCE(a.liberada, false) ORDER BY a.prazo_fim, a.id";
        List<AvaliacaoDaTurma> avaliacoes = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idTurma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp prazo = rs.getTimestamp("prazo_fim");
                    avaliacoes.add(new AvaliacaoDaTurma(rs.getInt("id"), rs.getString("titulo"),
                            rs.getBigDecimal("peso"), prazo == null ? null : prazo.toLocalDateTime()));
                }
            }
        }
        return avaliacoes;
    }

    public static List<AlunoDaTurma> alunosDaTurma(Connection c, int idTurma) throws SQLException {
        String sql = "SELECT u.id, u.nome, u.email FROM matriculas m JOIN usuarios u ON u.id = m.aluno_id "
                + "WHERE m.turma_id = ? ORDER BY u.nome, u.id";
        List<AlunoDaTurma> alunos = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idTurma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alunos.add(new AlunoDaTurma(rs.getInt("id"), rs.getString("nome"), rs.getString("email")));
                }
            }
        }
        return alunos;
    }

    public static List<TentativaRegistrada> tentativasDaTurma(Connection c, int idTurma) throws SQLException {
        String sql = "SELECT t.aluno_id, t.avaliacao_id, t.status::text AS status, "
                + "t.nota_total / NULLIF(a.peso, 0) AS nota_total, "
                + "COALESCE(t.corrigida_completamente, false) AS corrigida, "
                + "COALESCE(t.corrigida_completamente, false) AS finalizada "
                + "FROM avaliacao_turmas avt JOIN avaliacoes a ON a.id = avt.avaliacao_id "
                + "JOIN tentativas_avaliacao t ON t.avaliacao_id = a.id "
                + "WHERE avt.turma_id = ? AND COALESCE(a.liberada, false)";
        List<TentativaRegistrada> tentativas = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idTurma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tentativas.add(new TentativaRegistrada(rs.getInt("aluno_id"), rs.getInt("avaliacao_id"),
                            rs.getString("status"), rs.getBigDecimal("nota_total"), rs.getBoolean("corrigida"),
                            rs.getBoolean("finalizada")));
                }
            }
        }
        return tentativas;
    }

    public static boolean turmaConcluida(Connection c, int idTurma) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT NOT ativa FROM turmas WHERE id = ?")) {
            ps.setInt(1, idTurma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

}
