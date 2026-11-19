package br.snctech.notas.repositorio;

import br.snctech.banco.Conexao;
import br.snctech.notas.model.AlunoDaTurma;
import br.snctech.notas.model.AvaliacaoDaTurma;
import br.snctech.notas.model.MediaFinal;
import br.snctech.notas.model.MediaLiberada;
import br.snctech.notas.model.SituacaoFinal;
import br.snctech.notas.model.TentativaRegistrada;
import br.snctech.notas.model.TurmaResumo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositorioMediasPostgres implements RepositorioMedias {

    @Override
    public List<TurmaResumo> turmasDoProfessor(int idProfessor) {
        return Conexao.executar(c -> turmasDoProfessor(c, idProfessor));
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
    public void sincronizar(int idTurma, List<MediaFinal> mediasProntas) {
        Conexao.emTransacao(c -> {
            sincronizar(c, idTurma, mediasProntas);
            return null;
        });
    }

    @Override
    public Map<Integer, Boolean> liberacaoPorAluno(int idTurma) {
        return Conexao.executar(c -> liberacaoPorAluno(c, idTurma));
    }

    @Override
    public void liberar(int idTurma) {
        Conexao.executar(c -> {
            liberar(c, idTurma);
            return null;
        });
    }

    @Override
    public Integer turmaDaAvaliacao(int idAvaliacao) {
        return Conexao.executar(c -> turmaDaAvaliacao(c, idAvaliacao));
    }

    @Override
    public List<MediaLiberada> mediasLiberadasDoAluno(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }
        return Conexao.executar(c -> mediasLiberadasDoAluno(c, email.trim()));
    }

    public static boolean tabelaExiste(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT to_regclass('public.medias_finais') IS NOT NULL");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getBoolean(1);
        }
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

    public static List<AvaliacaoDaTurma> avaliacoesDaTurma(Connection c, int idTurma) throws SQLException {
        String sql = "SELECT id, titulo, peso, prazo_fim FROM avaliacoes "
                + "WHERE turma_id = ? AND COALESCE(liberada, false) ORDER BY prazo_fim, id";
        List<AvaliacaoDaTurma> avaliacoes = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idTurma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp prazo = rs.getTimestamp("prazo_fim");
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
        String sql = "SELECT t.aluno_id, t.avaliacao_id, t.status::text AS status, t.nota_total, "
                + "COALESCE(t.corrigida_completamente, false) AS corrigida, "
                + "COALESCE(t.correcao_finalizada, false) AS finalizada "
                + "FROM tentativas_avaliacao t JOIN avaliacoes a ON a.id = t.avaliacao_id "
                + "WHERE a.turma_id = ? AND COALESCE(a.liberada, false)";
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

    public static void sincronizar(Connection c, int idTurma, List<MediaFinal> mediasProntas) throws SQLException {
        String upsert = "INSERT INTO medias_finais (turma_id, aluno_id, media, situacao, calculada_em) "
                + "VALUES (?, ?, ?, ?, now()) ON CONFLICT (turma_id, aluno_id) DO UPDATE SET "
                + "media = EXCLUDED.media, situacao = EXCLUDED.situacao, calculada_em = EXCLUDED.calculada_em";
        try (PreparedStatement ps = c.prepareStatement(upsert)) {
            for (MediaFinal media : mediasProntas) {
                if (!media.estaPronta()) {
                    continue;
                }
                ps.setInt(1, idTurma);
                ps.setInt(2, media.idAluno());
                ps.setBigDecimal(3, media.media());
                ps.setString(4, media.situacao().name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        Integer[] mantidos = mediasProntas.stream().filter(MediaFinal::estaPronta)
                .map(MediaFinal::idAluno).toArray(Integer[]::new);
        try (PreparedStatement ps = c.prepareStatement(
                "DELETE FROM medias_finais WHERE turma_id = ? AND NOT (aluno_id = ANY (?))")) {
            ps.setInt(1, idTurma);
            ps.setArray(2, c.createArrayOf("integer", mantidos));
            ps.executeUpdate();
        }
    }

    public static Map<Integer, Boolean> liberacaoPorAluno(Connection c, int idTurma) throws SQLException {
        Map<Integer, Boolean> liberacao = new HashMap<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT aluno_id, liberada FROM medias_finais WHERE turma_id = ?")) {
            ps.setInt(1, idTurma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liberacao.put(rs.getInt("aluno_id"), rs.getBoolean("liberada"));
                }
            }
        }
        return liberacao;
    }

    public static void liberar(Connection c, int idTurma) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE medias_finais SET liberada = true, liberada_em = now() WHERE turma_id = ? AND NOT liberada")) {
            ps.setInt(1, idTurma);
            ps.executeUpdate();
        }
    }

    public static Integer turmaDaAvaliacao(Connection c, int idAvaliacao) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT turma_id FROM avaliacoes WHERE id = ?")) {
            ps.setInt(1, idAvaliacao);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int turma = rs.getInt(1);
                    return rs.wasNull() ? null : turma;
                }
                return null;
            }
        }
    }

    public static List<MediaLiberada> mediasLiberadasDoAluno(Connection c, String email) throws SQLException {
        String sql = "SELECT t.nome, mf.media, mf.situacao FROM medias_finais mf "
                + "JOIN turmas t ON t.id = mf.turma_id JOIN usuarios u ON u.id = mf.aluno_id "
                + "WHERE lower(u.email) = lower(?) AND mf.liberada ORDER BY t.nome";
        List<MediaLiberada> medias = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    medias.add(new MediaLiberada(rs.getString("nome"), rs.getBigDecimal("media"),
                            SituacaoFinal.valueOf(rs.getString("situacao"))));
                }
            }
        }
        return medias;
    }
}
