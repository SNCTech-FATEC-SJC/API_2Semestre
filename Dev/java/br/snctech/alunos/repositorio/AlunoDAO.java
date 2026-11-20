package br.snctech.alunos.repositorio;

import br.snctech.alunos.modelo.Aluno;
import br.snctech.turmas.modelo.Turma;
import br.snctech.turmas.repositorio.TurmaDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a dados dos alunos (usuarios com perfil "aluno") e das matrículas (tabela matriculas).
 * Todos os métodos recebem a Connection (Conexao.executar / Conexao.emTransacao).
 *
 * Atenção: por decisão do projeto, a senha é gravada como texto simples em senha_hash.
 */
public class AlunoDAO {

    /** Valor do enum perfil_usuario que identifica o aluno. */
    public static final String PERFIL_ALUNO = "aluno";

    private static final String SELECT_TURMA_DO_ALUNO =
            "SELECT t.id, t.nome, t.professor_id, COALESCE(p.nome, '') AS professor_nome, t.ativa, t.criado_em, "
                    + "(SELECT COUNT(*) FROM matriculas x WHERE x.turma_id = t.id) AS total_alunos "
                    + "FROM turmas t LEFT JOIN usuarios p ON p.id = t.professor_id ";

    // ------------------------------------------------------------------ alunos

    public static List<Aluno> listar(Connection conexao) throws SQLException {
        String sql = "SELECT u.id, u.nome, u.email, u.criado_em, "
                + "(SELECT string_agg(t.nome, ', ' ORDER BY t.nome) FROM matriculas m "
                + "   JOIN turmas t ON t.id = m.turma_id WHERE m.aluno_id = u.id AND t.ativa) AS turma_atual "
                + "FROM usuarios u WHERE lower(u.perfil::text) = '" + PERFIL_ALUNO + "' ORDER BY lower(u.nome)";
        try (PreparedStatement ps = conexao.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Aluno> alunos = new ArrayList<>();
            while (rs.next()) {
                Timestamp criado = rs.getTimestamp("criado_em");
                alunos.add(new Aluno(rs.getInt("id"), rs.getString("nome"), rs.getString("email"),
                        criado == null ? null : criado.toLocalDateTime(), rs.getString("turma_atual")));
            }
            return alunos;
        }
    }

    /** true se outro usuário (id diferente de ignorarId) já usa o e-mail. Use ignorarId = 0 no cadastro. */
    public static boolean emailExiste(Connection conexao, String email, int ignorarId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(
                "SELECT 1 FROM usuarios WHERE lower(email) = lower(?) AND id <> ?")) {
            ps.setString(1, email);
            ps.setInt(2, ignorarId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Cadastra o aluno e retorna o id novo. */
    public static int cadastrar(Connection conexao, String nome, String email, String senha) throws SQLException {
        if (emailExiste(conexao, email, 0)) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.");
        }
        try (PreparedStatement ps = conexao.prepareStatement(
                "INSERT INTO usuarios (nome, email, senha_hash, perfil, criado_em) "
                        + "VALUES (?, ?, ?, CAST(? AS perfil_usuario), CURRENT_TIMESTAMP) RETURNING id")) {
            ps.setString(1, nome);
            ps.setString(2, email);
            ps.setString(3, senha);
            ps.setString(4, PERFIL_ALUNO);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Edita o aluno. Senha null ou em branco mantém a senha atual. */
    public static int atualizar(Connection conexao, int alunoId, String nome, String email, String senha)
            throws SQLException {
        if (emailExiste(conexao, email, alunoId)) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.");
        }
        boolean trocarSenha = senha != null && !senha.isBlank();
        String sql = "UPDATE usuarios SET nome = ?, email = ?" + (trocarSenha ? ", senha_hash = ?" : "")
                + " WHERE id = ? AND lower(perfil::text) = '" + PERFIL_ALUNO + "'";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, nome);
            ps.setString(i++, email);
            if (trocarSenha) {
                ps.setString(i++, senha);
            }
            ps.setInt(i, alunoId);
            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                throw new IllegalArgumentException("O aluno não foi encontrado (pode ter sido removido).");
            }
            return linhas;
        }
    }

    /**
     * Exclui o aluno (DELETE) e suas matrículas. Se ele tiver histórico de avaliações, o banco
     * recusa (SQLState 23503) e nada é apagado.
     */
    public static int remover(Connection conexao, int alunoId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("DELETE FROM matriculas WHERE aluno_id = ?")) {
            ps.setInt(1, alunoId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conexao.prepareStatement(
                "DELETE FROM usuarios WHERE id = ? AND lower(perfil::text) = '" + PERFIL_ALUNO + "'")) {
            ps.setInt(1, alunoId);
            return ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ matrículas

    /** Todas as turmas do aluno (ativas e concluídas). */
    public static List<Turma> listarTurmasDoAluno(Connection conexao, int alunoId) throws SQLException {
        String sql = SELECT_TURMA_DO_ALUNO + "JOIN matriculas m ON m.turma_id = t.id "
                + "WHERE m.aluno_id = ? ORDER BY t.ativa DESC, t.criado_em DESC, t.id DESC";
        return consultarTurmas(conexao, sql, alunoId);
    }

    /** Turmas ativas em que o aluno ainda não está matriculado. */
    public static List<Turma> listarTurmasAtivasDisponiveis(Connection conexao, int alunoId) throws SQLException {
        String sql = SELECT_TURMA_DO_ALUNO + "WHERE t.ativa = TRUE AND NOT EXISTS "
                + "(SELECT 1 FROM matriculas m WHERE m.turma_id = t.id AND m.aluno_id = ?) ORDER BY lower(t.nome)";
        return consultarTurmas(conexao, sql, alunoId);
    }

    private static List<Turma> consultarTurmas(Connection conexao, String sql, int alunoId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, alunoId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Turma> turmas = new ArrayList<>();
                while (rs.next()) {
                    turmas.add(TurmaDAO.mapear(rs));
                }
                return turmas;
            }
        }
    }

    /** Matricula o aluno em uma turma ATIVA. */
    public static int adicionarATurma(Connection conexao, int alunoId, int turmaId) throws SQLException {
        exigirTurmaAtiva(conexao, turmaId, "Só é possível adicionar alunos a turmas ativas.");
        try (PreparedStatement ps = conexao.prepareStatement(
                "INSERT INTO matriculas (turma_id, aluno_id, criado_em) "
                        + "SELECT CAST(? AS int), CAST(? AS int), CURRENT_TIMESTAMP "
                        + "WHERE NOT EXISTS (SELECT 1 FROM matriculas WHERE turma_id = ? AND aluno_id = ?)")) {
            ps.setInt(1, turmaId);
            ps.setInt(2, alunoId);
            ps.setInt(3, turmaId);
            ps.setInt(4, alunoId);
            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                throw new IllegalArgumentException("O aluno já está matriculado nesta turma.");
            }
            return linhas;
        }
    }

    /** Remove o aluno de uma turma ATIVA (turmas concluídas preservam o histórico). */
    public static int removerDaTurma(Connection conexao, int alunoId, int turmaId) throws SQLException {
        exigirTurmaAtiva(conexao, turmaId,
                "Só é possível remover o aluno de turmas ativas. O histórico das turmas concluídas é preservado.");
        try (PreparedStatement ps = conexao.prepareStatement(
                "DELETE FROM matriculas WHERE turma_id = ? AND aluno_id = ?")) {
            ps.setInt(1, turmaId);
            ps.setInt(2, alunoId);
            return ps.executeUpdate();
        }
    }

    private static void exigirTurmaAtiva(Connection conexao, int turmaId, String mensagem) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("SELECT ativa FROM turmas WHERE id = ?")) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean(1)) {
                    throw new IllegalArgumentException(mensagem);
                }
            }
        }
    }
}
