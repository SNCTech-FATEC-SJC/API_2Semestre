package br.snctech.Repositorio;

import br.snctech.Questao;
import br.snctech.banco.Conexao;
import br.snctech.banco.QuestaoDAO;
import br.snctech.banco.UsuarioDAO;
import br.snctech.model.Tentativa;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** Persistência PostgreSQL das tentativas e respostas dos alunos. */
public class RepositorioTentativasJdbc implements RepositorioTentativas {
    @Override public Tentativa salvar(int avaliacaoId, String aluno, LocalDateTime inicio, LocalDateTime limite) {
        return Conexao.emTransacao(c -> {
            UsuarioDAO.Usuario usuario = UsuarioDAO.buscarAlunoPorEmail(c, aluno);
            if (usuario == null) throw new IllegalArgumentException("Não existe aluno cadastrado com esse e-mail.");
            int id;
            String sql = "INSERT INTO tentativas_avaliacao (avaliacao_id,aluno_id,data_inicio,limite,status) VALUES (?,?,?,?,'em_andamento') RETURNING id";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, avaliacaoId); ps.setInt(2, usuario.id());
                ps.setTimestamp(3, Timestamp.valueOf(inicio)); ps.setTimestamp(4, Timestamp.valueOf(limite));
                try (ResultSet rs = ps.executeQuery()) { rs.next(); id = rs.getInt(1); }
            }
            return carregar(c, id);
        });
    }

    @Override public Tentativa buscarPorId(int id) { return Conexao.executar(c -> carregar(c, id)); }

    @Override public Tentativa buscarPorAvaliacaoEAluno(int avaliacaoId, String aluno) {
        return Conexao.executar(c -> {
            String sql = "SELECT ta.id FROM tentativas_avaliacao ta JOIN usuarios u ON u.id=ta.aluno_id WHERE ta.avaliacao_id=? AND lower(u.email)=lower(?) LIMIT 1";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, avaliacaoId); ps.setString(2, aluno == null ? "" : aluno.trim());
                try (ResultSet rs = ps.executeQuery()) { return rs.next() ? carregar(c, rs.getInt(1)) : null; }
            }
        });
    }

    @Override public List<Tentativa> listarPorAvaliacao(int avaliacaoId) {
        return Conexao.executar(c -> {
            List<Tentativa> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM tentativas_avaliacao WHERE avaliacao_id=? ORDER BY id")) {
                ps.setInt(1, avaliacaoId);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) lista.add(carregar(c, rs.getInt(1))); }
            }
            return lista;
        });
    }

    @Override public void registrarResposta(Tentativa tentativa, Questao questao, String resposta) {
        Conexao.emTransacao(c -> {
            if (resposta == null || resposta.isBlank()) {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM respostas_aluno WHERE tentativa_id=? AND questao_id=?")) {
                    ps.setInt(1, tentativa.getId()); ps.setInt(2, questao.getId()); ps.executeUpdate();
                }
            } else {
                Integer alternativa = null; String discursiva = null, associacao = null;
                switch (questao.getTipo()) {
                    case "Múltipla Escolha", "Verdadeiro/Falso" -> alternativa = QuestaoDAO.buscarIdDaAlternativa(c, questao.getId(), resposta);
                    case "Ligar Colunas" -> associacao = resposta;
                    default -> discursiva = resposta;
                }
                String sql = "INSERT INTO respostas_aluno (tentativa_id,questao_id,alternativa_escolhida_id,resposta_discursiva,resposta_associacao) VALUES (?,?,?,?,?) ON CONFLICT (tentativa_id,questao_id) DO UPDATE SET alternativa_escolhida_id=EXCLUDED.alternativa_escolhida_id,resposta_discursiva=EXCLUDED.resposta_discursiva,resposta_associacao=EXCLUDED.resposta_associacao";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, tentativa.getId()); ps.setInt(2, questao.getId());
                    if (alternativa == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, alternativa);
                    ps.setString(4, discursiva); ps.setString(5, associacao); ps.executeUpdate();
                }
            }
            tentativa.registrarResposta(questao, resposta);
            return null;
        });
    }

    @Override public void concluir(Tentativa tentativa, Tentativa.Status status, LocalDateTime momento) {
        Conexao.executar(c -> {
            // O enum do banco usa nao_realizada_prazo para uma tentativa encerrada sem entrega.
            String statusBanco = switch (status) {
                case EM_ANDAMENTO -> "em_andamento";
                case FINALIZADA -> "enviada";
                case ENCERRADA -> "nao_realizada_prazo";
            };
            try (PreparedStatement ps = c.prepareStatement("UPDATE tentativas_avaliacao SET status=CAST(? AS status_tentativa),data_envio=? WHERE id=?")) {
                ps.setString(1, statusBanco); if (status == Tentativa.Status.EM_ANDAMENTO) ps.setNull(2, Types.TIMESTAMP); else ps.setTimestamp(2, Timestamp.valueOf(momento)); ps.setInt(3, tentativa.getId());
                if (ps.executeUpdate() != 1) throw new SQLException("Tentativa não encontrada: " + tentativa.getId());
            }
            tentativa.concluir(status, momento);
            return null;
        });
    }

    @Override public boolean alunoExiste(String aluno) {
        if (aluno == null || aluno.isBlank()) return false;
        return Conexao.executar(c -> UsuarioDAO.buscarAlunoPorEmail(c, aluno) != null);
    }

    private static Tentativa carregar(Connection c, int id) throws SQLException {
        String sql = "SELECT ta.id,ta.avaliacao_id,ta.data_inicio,ta.limite,ta.data_envio,ta.status,u.email FROM tentativas_avaliacao ta JOIN usuarios u ON u.id=ta.aluno_id WHERE ta.id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                LocalDateTime inicio = rs.getTimestamp("data_inicio").toLocalDateTime();
                Timestamp limiteTs = rs.getTimestamp("limite");
                LocalDateTime limite = limiteTs == null ? inicio : limiteTs.toLocalDateTime();
                Tentativa t = new Tentativa(rs.getInt("id"), rs.getInt("avaliacao_id"), rs.getString("email"), inicio, limite);
                String status = rs.getString("status");
                if (!"em_andamento".equals(status)) {
                    Timestamp fim = rs.getTimestamp("data_envio");
                    t.concluir("enviada".equals(status) ? Tentativa.Status.FINALIZADA : Tentativa.Status.ENCERRADA,
                            fim == null ? limite : fim.toLocalDateTime());
                }
                Map<Integer, Questao> questoes = QuestaoDAO.carregarDeTentativa(c, id);
                String respostaSql = "SELECT ra.questao_id,ra.alternativa_escolhida_id,ra.resposta_discursiva,ra.resposta_associacao,a.texto FROM respostas_aluno ra LEFT JOIN alternativas a ON a.id=ra.alternativa_escolhida_id WHERE ra.tentativa_id=? ORDER BY ra.id";
                try (PreparedStatement rps = c.prepareStatement(respostaSql)) {
                    rps.setInt(1, id);
                    try (ResultSet rr = rps.executeQuery()) {
                        while (rr.next()) {
                            Questao q = questoes.get(rr.getInt("questao_id")); if (q == null) continue;
                            String resposta = rr.getString("texto");
                            if (resposta == null) resposta = rr.getString("resposta_discursiva");
                            if (resposta == null) resposta = rr.getString("resposta_associacao");
                            t.registrarResposta(q, resposta);
                        }
                    }
                }
                return t;
            }
        }
    }
}
