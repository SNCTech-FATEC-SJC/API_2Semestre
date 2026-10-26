package br.snctech.correcao.repositorio.postgres;

import br.snctech.banco.Conexao;
import br.snctech.correcao.model.AvaliacaoRespondida;
import br.snctech.correcao.model.RespostaQuestao;
import br.snctech.correcao.model.StatusCorrecao;
import br.snctech.correcao.repositorio.RepositorioAvaliacoesRespondidas;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Provas respondidas e suas notas, no PostgreSQL.
 *
 * <ul>
 *   <li>Uma prova respondida é uma linha de {@code tentativas_avaliacao} (só entram as já entregues:
 *       status {@code enviada} ou {@code nao_realizada_prazo}; {@code em_andamento} ainda não pode ser corrigida).</li>
 *   <li>Cada resposta é uma linha de {@code respostas_aluno}. As colunas da resposta do aluno
 *       (alternativa escolhida, texto, associação) são gravadas pela tela de realização e <b>nunca são alteradas aqui</b>;
 *       a correção só grava nota, status, nota sugerida e quem corrigiu.</li>
 *   <li>{@code salvar} grava também {@code nota_total} e {@code corrigida_completamente} da tentativa.</li>
 * </ul>
 * Precisa da migração 02 (colunas {@code status_correcao}, {@code nota_sugerida}, {@code corrigido_por}).
 */
public class RepositorioAvaliacoesRespondidasPostgres implements RepositorioAvaliacoesRespondidas {

    @Override
    public AvaliacaoRespondida buscar(Integer id) {
        return Conexao.executar(c -> {
            AvaliacaoRespondida prova = carregar(c, id);
            if (prova == null) {
                throw new IllegalArgumentException("Não existe prova respondida com o id " + id);
            }
            return prova;
        });
    }

    @Override
    public List<AvaliacaoRespondida> buscarPorAvaliacao(Integer idAvaliacao) {
        return Conexao.executar(c -> {
            List<Integer> ids = new ArrayList<>();
            String sql = "SELECT id FROM tentativas_avaliacao WHERE avaliacao_id = ? "
                    + "AND status IN ('enviada', 'nao_realizada_prazo') ORDER BY id";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, idAvaliacao);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ids.add(rs.getInt(1));
                    }
                }
            }
            List<AvaliacaoRespondida> provas = new ArrayList<>();
            for (int id : ids) {
                provas.add(carregar(c, id));
            }
            return provas;
        });
    }

    @Override
    public void salvar(AvaliacaoRespondida prova) {
        Conexao.emTransacao(c -> {
            String sqlResposta = "INSERT INTO respostas_aluno "
                    + "(tentativa_id, questao_id, nota_atribuida, validada, status_correcao, nota_sugerida, corrigido_por) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                    + "ON CONFLICT (tentativa_id, questao_id) DO UPDATE SET "
                    + "nota_atribuida = EXCLUDED.nota_atribuida, validada = EXCLUDED.validada, "
                    + "status_correcao = EXCLUDED.status_correcao, nota_sugerida = EXCLUDED.nota_sugerida, "
                    + "corrigido_por = EXCLUDED.corrigido_por";
            try (PreparedStatement ps = c.prepareStatement(sqlResposta)) {
                for (RespostaQuestao r : prova.respostas().values()) {
                    StatusCorrecao status = r.getStatus();
                    ps.setInt(1, prova.getId());
                    ps.setInt(2, r.getIdQuestao());
                    ps.setBigDecimal(3, r.getNota() == null ? BigDecimal.ZERO : r.getNota());
                    ps.setBoolean(4, status != null && status != StatusCorrecao.PENDENTE_MANUAL);
                    ps.setString(5, status == null ? null : status.name());
                    ps.setBigDecimal(6, r.getNotaSugerida());
                    if (r.getCorrigidoPor() == null) {
                        ps.setNull(7, Types.INTEGER);
                    } else {
                        ps.setInt(7, r.getCorrigidoPor());
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE tentativas_avaliacao SET nota_total = ?, corrigida_completamente = ? WHERE id = ?")) {
                ps.setBigDecimal(1, prova.notaTotal());
                ps.setBoolean(2, prova.notaFinalConfirmada());
                ps.setInt(3, prova.getId());
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Tentativa não encontrada: " + prova.getId());
                }
            }
            return null;
        });
    }

    private static AvaliacaoRespondida carregar(Connection c, int id) throws SQLException {
        AvaliacaoRespondida prova;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, avaliacao_id, aluno_id FROM tentativas_avaliacao WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                prova = new AvaliacaoRespondida(rs.getInt("id"), rs.getInt("avaliacao_id"), rs.getInt("aluno_id"));
            }
        }

        String sql = "SELECT questao_id, alternativa_escolhida_id, resposta_discursiva, resposta_associacao, "
                + "nota_atribuida, status_correcao, nota_sugerida, corrigido_por "
                + "FROM respostas_aluno WHERE tentativa_id = ? ORDER BY id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RespostaQuestao r = new RespostaQuestao(rs.getInt("questao_id"));
                    int alternativa = rs.getInt("alternativa_escolhida_id");
                    if (!rs.wasNull()) {
                        r.setAlternativaId(String.valueOf(alternativa));
                    }
                    r.setTexto(rs.getString("resposta_discursiva"));
                    r.setPares(FormatoAssociacao.ler(rs.getString("resposta_associacao")));

                    String status = rs.getString("status_correcao");
                    if (status != null) {
                        r.setStatus(StatusCorrecao.valueOf(status));
                        r.setNota(rs.getBigDecimal("nota_atribuida"));
                    }
                    r.setNotaSugerida(rs.getBigDecimal("nota_sugerida"));
                    int professor = rs.getInt("corrigido_por");
                    if (!rs.wasNull()) {
                        r.setCorrigidoPor(professor);
                    }
                    prova.adicionar(r);
                }
            }
        }
        return prova;
    }
}
