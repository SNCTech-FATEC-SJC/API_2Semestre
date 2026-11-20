package br.snctech.turmas.repositorio;

import br.snctech.turmas.modelo.AvaliacaoTurma;
import br.snctech.turmas.modelo.Membro;
import br.snctech.turmas.modelo.Professor;
import br.snctech.turmas.modelo.Turma;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a dados das turmas. Todos os métodos recebem a Connection (use Conexao.executar para
 * leituras e Conexao.emTransacao para gravações).
 * Regra: um professor pode ter várias turmas ativas ao mesmo tempo. Turma ativa = em andamento;
 * turma não ativa = concluída (histórico). Nenhuma operação conclui turmas por conta própria.
 */
public class TurmaDAO {

    private static final String SELECT_TURMA =
            "SELECT t.id, t.nome, t.professor_id, COALESCE(p.nome, '') AS professor_nome, t.ativa, t.criado_em, "
                    + "(SELECT COUNT(*) FROM matriculas m WHERE m.turma_id = t.id) AS total_alunos "
                    + "FROM turmas t LEFT JOIN usuarios p ON p.id = t.professor_id ";

    /** Converte uma linha com as colunas: id, nome, professor_id, professor_nome, ativa, criado_em, total_alunos. */
    public static Turma mapear(ResultSet rs) throws SQLException {
        Timestamp criado = rs.getTimestamp("criado_em");
        return new Turma(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getInt("professor_id"),
                rs.getString("professor_nome"),
                rs.getBoolean("ativa"),
                criado == null ? null : criado.toLocalDateTime(),
                rs.getInt("total_alunos"));
    }

    /** Lista turmas. professorId null = todas as turmas (uso do administrador). */
    public static List<Turma> listar(Connection conexao, Integer professorId) throws SQLException {
        String sql = SELECT_TURMA
                + (professorId != null ? "WHERE t.professor_id = ? " : "")
                + "ORDER BY t.ativa DESC, t.criado_em DESC, t.id DESC";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            if (professorId != null) {
                ps.setInt(1, professorId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Turma> turmas = new ArrayList<>();
                while (rs.next()) {
                    turmas.add(mapear(rs));
                }
                return turmas;
            }
        }
    }

    /** Turma pelo id (ativa ou concluída), ou null se não existir. */
    public static Turma buscarPorId(Connection conexao, int turmaId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(SELECT_TURMA + "WHERE t.id = ?")) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Cadastra uma turma ativa. Não altera as demais turmas do professor. Retorna o id novo. */
    public static int cadastrar(Connection conexao, String nome, int professorId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(
                "INSERT INTO turmas (nome, professor_id, ativa) VALUES (?, ?, TRUE) RETURNING id")) {
            ps.setString(1, nome);
            ps.setInt(2, professorId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Edita nome e professor responsável. */
    public static int atualizar(Connection conexao, int turmaId, String nome, int professorId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(
                "UPDATE turmas SET nome = ?, professor_id = ? WHERE id = ?")) {
            ps.setString(1, nome);
            ps.setInt(2, professorId);
            ps.setInt(3, turmaId);
            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                throw new IllegalArgumentException("A turma não foi encontrada (pode ter sido removida).");
            }
            return linhas;
        }
    }

    /** Conclui a turma (ativa = false). */
    public static int concluir(Connection conexao, int turmaId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("UPDATE turmas SET ativa = FALSE WHERE id = ?")) {
            ps.setInt(1, turmaId);
            return ps.executeUpdate();
        }
    }

    /** Reativa uma turma concluída. Não altera as demais turmas do professor. */
    public static int reativar(Connection conexao, int turmaId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("UPDATE turmas SET ativa = TRUE WHERE id = ?")) {
            ps.setInt(1, turmaId);
            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                throw new IllegalArgumentException("A turma não foi encontrada (pode ter sido removida).");
            }
            return linhas;
        }
    }

    public static List<Professor> listarProfessores(Connection conexao) throws SQLException {
        String sql = "SELECT id, nome FROM usuarios WHERE lower(perfil::text) = 'professor' ORDER BY lower(nome)";
        try (PreparedStatement ps = conexao.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Professor> professores = new ArrayList<>();
            while (rs.next()) {
                professores.add(new Professor(rs.getInt("id"), rs.getString("nome")));
            }
            return professores;
        }
    }

    /** Perfil do usuário em texto (professor, aluno, admin) ou null se não existir. */
    public static String perfilDoUsuario(Connection conexao, int usuarioId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("SELECT perfil::text FROM usuarios WHERE id = ?")) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public static List<Membro> listarMembros(Connection conexao, int turmaId) throws SQLException {
        String sql = "SELECT u.id, u.nome, u.email, m.criado_em FROM matriculas m "
                + "JOIN usuarios u ON u.id = m.aluno_id WHERE m.turma_id = ? ORDER BY lower(u.nome)";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Membro> membros = new ArrayList<>();
                while (rs.next()) {
                    Timestamp data = rs.getTimestamp("criado_em");
                    membros.add(new Membro(rs.getInt("id"), rs.getString("nome"), rs.getString("email"),
                            data == null ? null : data.toLocalDateTime()));
                }
                return membros;
            }
        }
    }

    /**
     * Avaliações da turma e quantos alunos matriculados já enviaram cada uma
     * (tentativa com data_envio preenchida).
     */
    public static List<AvaliacaoTurma> listarAvaliacoes(Connection conexao, int turmaId) throws SQLException {
        String sql = "SELECT a.id, a.titulo, "
                + "(SELECT COUNT(*) FROM matriculas m WHERE m.turma_id = a.turma_id) AS total, "
                + "(SELECT COUNT(DISTINCT t.aluno_id) FROM tentativas_avaliacao t "
                + "   JOIN matriculas m ON m.aluno_id = t.aluno_id AND m.turma_id = a.turma_id "
                + "  WHERE t.avaliacao_id = a.id AND t.data_envio IS NOT NULL) AS concluiram "
                + "FROM avaliacoes a WHERE a.turma_id = ? ORDER BY a.id";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                List<AvaliacaoTurma> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new AvaliacaoTurma(rs.getInt("id"), rs.getString("titulo"),
                            rs.getInt("concluiram"), rs.getInt("total")));
                }
                return lista;
            }
        }
    }

    // ------------------------------------------------------------------ compatibilidade

    /** @deprecated use {@link #cadastrar} dentro de Conexao.emTransacao. Mantido para código antigo. */
    @Deprecated
    public static void cadastrarTurma(Connection conexao, int professorId, String nomeTurma) {
        try {
            boolean autoCommitAnterior = conexao.getAutoCommit();
            conexao.setAutoCommit(false);
            try {
                cadastrar(conexao, nomeTurma, professorId);
                conexao.commit();
            } catch (SQLException | RuntimeException e) {
                conexao.rollback();
                throw e;
            } finally {
                conexao.setAutoCommit(autoCommitAnterior);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao gerenciar turmas: " + e.getMessage());
        }
    }

    /** @deprecated use {@link #listar}. Mantido para código antigo. */
    @Deprecated
    public static void listarTurmasDoProfessor(Connection conexao, int professorId) {
        try {
            System.out.println("\n--- Histórico de Turmas do Professor ---");
            for (Turma t : listar(conexao, professorId)) {
                System.out.printf("ID: %d | Turma: %s | Status: %s | Criada em: %s%n",
                        t.id(), t.nome(), t.ativa() ? "[ATIVA]" : "[ENCERRADA]", t.criadoEm());
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar turmas: " + e.getMessage());
        }
    }
}
