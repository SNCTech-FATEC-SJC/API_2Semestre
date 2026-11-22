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
 * Regra: cada professor tem UMA turma ativa (a atual); as demais são turmas concluídas (histórico).
 */
public class TurmaDAO {

    private static final String PREFIXO_RECUPERACAO = "Recuperação turma #";
    private record TurmaConcluida(int id, String nome) { }

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

    /** Cadastra uma turma ativa e conclui a turma ativa anterior do professor. Retorna o id novo. */
    public static int cadastrar(Connection conexao, String nome, int professorId) throws SQLException {
        encerrarOutrasAtivas(conexao, professorId, -1);
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

    /** Edita nome e professor. Se a turma é ativa e o professor mudou, a ativa anterior dele é concluída. */
    public static int atualizar(Connection conexao, int turmaId, String nome, int professorId) throws SQLException {
        boolean ativa;
        try (PreparedStatement ps = conexao.prepareStatement("SELECT ativa FROM turmas WHERE id = ?")) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("A turma não foi encontrada (pode ter sido removida).");
                }
                ativa = rs.getBoolean(1);
            }
        }
        int linhas;
        try (PreparedStatement ps = conexao.prepareStatement(
                "UPDATE turmas SET nome = ?, professor_id = ? WHERE id = ?")) {
            ps.setString(1, nome);
            ps.setInt(2, professorId);
            ps.setInt(3, turmaId);
            linhas = ps.executeUpdate();
        }
        if (ativa) {
            encerrarOutrasAtivas(conexao, professorId, turmaId);
        }
        return linhas;
    }

    /** Conclui a turma (ativa = false). */
    public static int concluir(Connection conexao, int turmaId) throws SQLException {
        String nome;
        try (PreparedStatement ps = conexao.prepareStatement("SELECT nome FROM turmas WHERE id = ?")) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("A turma não foi encontrada (pode ter sido removida).");
                }
                nome = rs.getString("nome");
            }
        }
        int linhas;
        try (PreparedStatement ps = conexao.prepareStatement("UPDATE turmas SET ativa = FALSE WHERE id = ?")) {
            ps.setInt(1, turmaId);
            linhas = ps.executeUpdate();
        }
        RecuperacaoTurmaDAO.criarSeNecessario(conexao, turmaId, nome);
        return linhas;
    }

    /** Reavalia as turmas concluídas quando uma correção altera a nota de uma avaliação. */
    public static void atualizarRecuperacoesDaAvaliacao(Connection conexao, int avaliacaoId) throws SQLException {
        String sql = "SELECT DISTINCT t.id, t.nome FROM turmas t "
                + "JOIN avaliacao_turmas avt ON avt.turma_id = t.id "
                + "WHERE avt.avaliacao_id = ? AND NOT t.ativa";
        List<TurmaConcluida> turmasConcluidas = new ArrayList<>();
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, avaliacaoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    turmasConcluidas.add(new TurmaConcluida(rs.getInt("id"), rs.getString("nome")));
                }
            }
        }
        for (TurmaConcluida turma : turmasConcluidas) {
            RecuperacaoTurmaDAO.criarSeNecessario(conexao, turma.id(), turma.nome());
        }
    }

    /** Reativa uma turma concluída e conclui a turma ativa atual do mesmo professor. */
    public static int reativar(Connection conexao, int turmaId) throws SQLException {
        int professorId;
        try (PreparedStatement ps = conexao.prepareStatement("SELECT professor_id FROM turmas WHERE id = ?")) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("A turma não foi encontrada (pode ter sido removida).");
                }
                professorId = rs.getInt(1);
            }
        }
        encerrarOutrasAtivas(conexao, professorId, turmaId);
        try (PreparedStatement ps = conexao.prepareStatement("UPDATE turmas SET ativa = TRUE WHERE id = ?")) {
            ps.setInt(1, turmaId);
            return ps.executeUpdate();
        }
    }

    private static void encerrarOutrasAtivas(Connection conexao, int professorId, int exceto) throws SQLException {
        List<TurmaConcluida> turmasEncerradas = new ArrayList<>();
        try (PreparedStatement ps = conexao.prepareStatement(
                "SELECT id, nome FROM turmas WHERE professor_id = ? AND ativa = TRUE AND id <> ?")) {
            ps.setInt(1, professorId);
            ps.setInt(2, exceto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    turmasEncerradas.add(new TurmaConcluida(rs.getInt("id"), rs.getString("nome")));
                }
            }
        }
        try (PreparedStatement ps = conexao.prepareStatement(
                "UPDATE turmas SET ativa = FALSE WHERE professor_id = ? AND ativa = TRUE AND id <> ?")) {
            ps.setInt(1, professorId);
            ps.setInt(2, exceto);
            ps.executeUpdate();
        }
        for (TurmaConcluida turma : turmasEncerradas) {
            RecuperacaoTurmaDAO.criarSeNecessario(conexao, turma.id(), turma.nome());
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
