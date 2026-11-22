package br.snctech.correcao.repositorio.postgres;

import br.snctech.banco.Conexao;
import br.snctech.correcao.model.NotaDoAluno;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

/**
 * Lê do PostgreSQL as notas de um aluno (identificado pelo e-mail, como no resto do sistema).
 * Entram as avaliações liberadas da turma dele que já foram abertas por ele ou que já encerraram.
 */
public final class NotasDoAlunoPostgres {

    private NotasDoAlunoPostgres() {
    }

    /** Lista vazia se o e-mail não existe ou se o sistema está em modo memória (sem banco). */
    public static List<NotaDoAluno> listar(String emailDoAluno) {
        if (emailDoAluno == null || emailDoAluno.isBlank() || Conexao.modoMemoria()) {
            return List.of();
        }
        return Conexao.executar(c -> {
            String sql = "SELECT DISTINCT a.id, a.titulo, a.prazo_fim, t.status::text AS status, t.nota_total, "
                    + "COALESCE(t.corrigida_completamente, false) AS corrigida "
                    + "FROM usuarios u "
                    + "JOIN matriculas m ON m.aluno_id = u.id "
                    + "JOIN avaliacao_turmas avt ON avt.turma_id = m.turma_id "
                    + "JOIN avaliacoes a ON a.id = avt.avaliacao_id AND COALESCE(a.liberada, false) "
                    + "LEFT JOIN tentativas_avaliacao t ON t.avaliacao_id = a.id AND t.aluno_id = u.id "
                    + "WHERE lower(u.email) = lower(?) AND (t.id IS NOT NULL OR a.prazo_fim < now()) "
                    + "ORDER BY a.prazo_fim DESC, a.id DESC";
            List<NotaDoAluno> notas = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, emailDoAluno.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        notas.add(NotaDoAluno.de(rs.getInt("id"), rs.getString("titulo"),
                                rs.getTimestamp("prazo_fim").toLocalDateTime(), rs.getString("status"),
                                rs.getBigDecimal("nota_total"), rs.getBoolean("corrigida")));
                    }
                }
            }
            return notas;
        });
    }

    /** Detalhes das respostas e correção de uma avaliação específica do aluno. */
    public static List<String> detalhar(String emailDoAluno, int idAvaliacao) {
        if (emailDoAluno == null || emailDoAluno.isBlank() || Conexao.modoMemoria()) return List.of();
        return Conexao.executar(c -> {
            String sql = "SELECT q.id, q.enunciado, "
                    + "COALESCE(alt.texto, ra.resposta_discursiva, ra.resposta_associacao, 'Não respondida') AS resposta, "
                    + "ra.nota_atribuida, ra.status_correcao "
                    + "FROM usuarios u JOIN tentativas_avaliacao ta ON ta.aluno_id=u.id "
                    + "JOIN avaliacoes a ON a.id=ta.avaliacao_id "
                    + "JOIN avaliacao_questoes aq ON aq.avaliacao_id=a.id "
                    + "JOIN questoes q ON q.id=aq.questao_id "
                    + "LEFT JOIN respostas_aluno ra ON ra.tentativa_id=ta.id AND ra.questao_id=q.id "
                    + "LEFT JOIN alternativas alt ON alt.id=ra.alternativa_escolhida_id "
                    + "WHERE lower(u.email)=lower(?) AND a.id=? ORDER BY aq.ordem, q.id";
            List<String> itens = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, emailDoAluno.trim()); ps.setInt(2, idAvaliacao);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String status = rs.getString("status_correcao");
                        String nota = rs.getBigDecimal("nota_atribuida") == null ? "Aguardando correção"
                                : rs.getBigDecimal("nota_atribuida").toPlainString();
                        itens.add("Questão " + rs.getInt("id") + "\n" + rs.getString("enunciado")
                                + "\nResposta: " + rs.getString("resposta")
                                + "\nNota: " + nota + (status == null ? "" : "\nSituação: " + status));
                    }
                }
            }
            return itens;
        });
    }

}
