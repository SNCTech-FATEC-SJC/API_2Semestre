package br.snctech.Repositorio;

import br.snctech.Questao;
import br.snctech.banco.Conexao;
import br.snctech.banco.ErroBanco;
import br.snctech.banco.QuestaoDAO;
import br.snctech.banco.SessaoUsuario;
import br.snctech.model.Avaliacao;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/** Implementação PostgreSQL do repositório de avaliações. */
public class RepositorioAvaliacoesJdbc implements RepositorioAvaliacoes {
    @Override public Avaliacao salvar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo) {
        return salvar(nome, questoes, turma, data, prazo, LocalTime.MIDNIGHT, LocalTime.MIDNIGHT, 0);
    }

    @Override public Avaliacao salvar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                                     LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos) {
        return Conexao.emTransacao(c -> {
            int turmaId = obterOuCriarTurma(c, turma);
            LocalDateTime inicio = data.atTime(horaInicio == null ? LocalTime.MIDNIGHT : horaInicio);
            LocalDateTime fim = prazo.atTime(horaFim == null ? LocalTime.MIDNIGHT : horaFim);
            int id;
            String sql = "INSERT INTO avaliacoes (turma_id,titulo,peso,versao,liberada,prazo_fim,inicio,duracao_minutos) VALUES (?,?,1.0,1,false,?,?,?) RETURNING id";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, turmaId); ps.setString(2, nome); ps.setTimestamp(3, Timestamp.valueOf(fim));
                ps.setTimestamp(4, Timestamp.valueOf(inicio)); ps.setInt(5, duracaoMinutos);
                try (ResultSet rs = ps.executeQuery()) { rs.next(); id = rs.getInt(1); }
            }
            gravarQuestoes(c, id, questoes);
            return carregarPorId(c, id);
        });
    }

    @Override public Avaliacao salvarNovaVersao(Avaliacao original, String nome, List<Questao> questoes, String turma,
                                                LocalDate data, LocalDate prazo, LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos) {
        return Conexao.emTransacao(c -> {
            int turmaId = obterOuCriarTurma(c, turma);
            int versao = original.getVersao() + 1;
            try (PreparedStatement ps = c.prepareStatement("SELECT COALESCE(MAX(versao), ?) + 1 FROM avaliacoes WHERE avaliacao_pai_id = ?")) {
                ps.setInt(1, original.getVersao()); ps.setInt(2, original.getAvaliacaoPaiId() == null ? original.getId() : original.getAvaliacaoPaiId());
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) versao = rs.getInt(1); }
            }
            Integer pai = original.getAvaliacaoPaiId() == null ? original.getId() : original.getAvaliacaoPaiId();
            String sql = "INSERT INTO avaliacoes (turma_id,titulo,peso,versao,avaliacao_pai_id,liberada,prazo_fim,inicio,duracao_minutos) VALUES (?,?,1.0,?,?,false,?,?,?) RETURNING id";
            int id;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, turmaId); ps.setString(2, nome); ps.setInt(3, versao); ps.setInt(4, pai);
                ps.setTimestamp(5, Timestamp.valueOf(prazo.atTime(horaFim)));
                ps.setTimestamp(6, Timestamp.valueOf(data.atTime(horaInicio))); ps.setInt(7, duracaoMinutos);
                try (ResultSet rs = ps.executeQuery()) { rs.next(); id = rs.getInt(1); }
            }
            gravarQuestoes(c, id, questoes);
            return carregarPorId(c, id);
        });
    }

    @Override public List<Avaliacao> listarTodas() {
        return Conexao.executar(c -> {
            List<Integer> ids = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM avaliacoes ORDER BY id"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
            List<Avaliacao> lista = new ArrayList<>();
            for (int id : ids) { Avaliacao a = carregarPorId(c, id); if (a != null) lista.add(a); }
            return lista;
        });
    }

    @Override public Avaliacao buscarPorId(int id) { return Conexao.executar(c -> carregarPorId(c, id)); }

    @Override public void atualizar(Avaliacao a, String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                                    LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos) {
        Conexao.emTransacao(c -> {
            int turmaId = obterOuCriarTurma(c, turma);
            String sql = "UPDATE avaliacoes SET turma_id=?, titulo=?, prazo_fim=?, inicio=?, duracao_minutos=? WHERE id=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, turmaId); ps.setString(2, nome); ps.setTimestamp(3, Timestamp.valueOf(prazo.atTime(horaFim)));
                ps.setTimestamp(4, Timestamp.valueOf(data.atTime(horaInicio))); ps.setInt(5, duracaoMinutos); ps.setInt(6, a.getId());
                if (ps.executeUpdate() != 1) throw new SQLException("Avaliação não encontrada para atualização: " + a.getId());
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM avaliacao_questoes WHERE avaliacao_id=?")) { ps.setInt(1, a.getId()); ps.executeUpdate(); }
            gravarQuestoes(c, a.getId(), questoes);
            return null;
        });
        a.setNome(nome); a.setQuestoes(questoes); a.setTurma(turma); a.setData(data); a.setPrazo(prazo);
        a.setHoraInicio(horaInicio); a.setHoraFim(horaFim); a.setDuracaoMinutos(duracaoMinutos);
    }

    @Override public void atualizarDisponibilizacao(Avaliacao a, boolean disponibilizada) {
        Conexao.executar(c -> {
            String sql = "UPDATE avaliacoes SET liberada=?, liberada_em=? WHERE id=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setBoolean(1, disponibilizada);
                if (disponibilizada) ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now())); else ps.setNull(2, Types.TIMESTAMP);
                ps.setInt(3, a.getId());
                if (ps.executeUpdate() != 1) throw new SQLException("Avaliação não encontrada: " + a.getId());
            }
            return null;
        });
        a.setDisponibilizada(disponibilizada);
    }

    private static int obterOuCriarTurma(Connection c, String nome) throws SQLException {
        Integer professorId = UsuarioProfessor.id(c);
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM turmas WHERE lower(nome)=lower(?) ORDER BY id LIMIT 1")) {
            ps.setString(1, nome.trim()); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        }
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO turmas (nome, professor_id, ativa) VALUES (?,?,true) RETURNING id")) {
            ps.setString(1, nome.trim()); if (professorId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, professorId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    private static void gravarQuestoes(Connection c, int avaliacaoId, List<Questao> questoes) throws SQLException {
        int professor = SessaoUsuario.getProfessorId();
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO avaliacao_questoes (avaliacao_id,questao_id,ordem) VALUES (?,?,?)")) {
            int ordem = 1;
            for (Questao q : questoes) {
                int id = q.getId();
                if (id <= 0) { id = QuestaoDAO.inserir(c, q, professor); q.setId(id); }
                ps.setInt(1, avaliacaoId); ps.setInt(2, id); ps.setInt(3, ordem++); ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static Avaliacao carregarPorId(Connection c, int id) throws SQLException {
        String sql = "SELECT a.*, t.nome AS turma_nome FROM avaliacoes a LEFT JOIN turmas t ON t.id=a.turma_id WHERE a.id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Map<Integer, Questao> mapa = QuestaoDAO.carregarDeAvaliacao(c, id);
                List<Questao> qs = new ArrayList<>();
                try (PreparedStatement qps = c.prepareStatement("SELECT questao_id FROM avaliacao_questoes WHERE avaliacao_id=? ORDER BY ordem NULLS LAST, questao_id")) {
                    qps.setInt(1, id); try (ResultSet qrs = qps.executeQuery()) { while (qrs.next()) { Questao q = mapa.get(qrs.getInt(1)); if (q != null) qs.add(q); } }
                }
                Timestamp inicio = rs.getTimestamp("inicio"), fim = rs.getTimestamp("prazo_fim");
                Avaliacao a = new Avaliacao(rs.getInt("id"), rs.getString("titulo"), qs);
                a.setTurma(rs.getString("turma_nome"));
                if (inicio != null) { a.setData(inicio.toLocalDateTime().toLocalDate()); a.setHoraInicio(inicio.toLocalDateTime().toLocalTime()); }
                if (fim != null) { a.setPrazo(fim.toLocalDateTime().toLocalDate()); a.setHoraFim(fim.toLocalDateTime().toLocalTime()); }
                a.setDuracaoMinutos(rs.getInt("duracao_minutos")); a.setDisponibilizada(rs.getBoolean("liberada"));
                a.setVersao(rs.getInt("versao")); int pai = rs.getInt("avaliacao_pai_id"); if (!rs.wasNull()) a.setAvaliacaoPaiId(pai);
                try (PreparedStatement tps = c.prepareStatement("SELECT EXISTS(SELECT 1 FROM tentativas_avaliacao WHERE avaliacao_id=?)")) {
                    tps.setInt(1, id); try (ResultSet trs = tps.executeQuery()) { if (trs.next()) a.setRespondida(trs.getBoolean(1)); }
                }
                return a;
            }
        }
    }

    private static final class UsuarioProfessor {
        static Integer id(Connection c) throws SQLException {
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM usuarios WHERE perfil=CAST('professor' AS perfil_usuario) ORDER BY id LIMIT 1"); ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }
}
