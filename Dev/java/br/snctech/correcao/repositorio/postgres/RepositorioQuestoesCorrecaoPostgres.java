package br.snctech.correcao.repositorio.postgres;

import br.snctech.banco.Conexao;
import br.snctech.correcao.model.QuestaoCorrecao;
import br.snctech.correcao.model.TipoQuestao;
import br.snctech.correcao.model.ValoresDaAvaliacao;
import br.snctech.correcao.repositorio.RepositorioQuestoesCorrecao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Questões de uma avaliação, com gabarito e valor, lidas do PostgreSQL
 * (tabelas avaliacao_questoes, questoes e alternativas).
 *
 * <p>Como cada tipo está gravado (convenção do {@code QuestaoDAO} do grupo):
 * <ul>
 *   <li>múltipla escolha e verdadeiro/falso: uma linha em {@code alternativas} por opção; a certa tem {@code correta = true};</li>
 *   <li>associação: uma linha por par, {@code texto} = termo A e {@code par_associacao} = termo B;</li>
 *   <li>discursiva: uma linha com a resposta esperada.</li>
 * </ul>
 * O valor de cada questão vem de {@code avaliacao_questoes.valor}. Se alguma questão ficar sem valor,
 * os 10 pontos são divididos igualmente entre todas.
 */
public class RepositorioQuestoesCorrecaoPostgres implements RepositorioQuestoesCorrecao {

    @Override
    public List<QuestaoCorrecao> buscarPorAvaliacao(Integer idAvaliacao) {
        return Conexao.executar(c -> carregar(c, idAvaliacao));
    }

    private record Linha(int id, TipoQuestao tipo, BigDecimal valor) { }

    private record Alternativa(int id, String texto, boolean correta, String par) { }

    private static List<QuestaoCorrecao> carregar(Connection c, int idAvaliacao) throws SQLException {
        List<Linha> linhas = new ArrayList<>();
        String sqlQuestoes = "SELECT q.id, q.tipo::text AS tipo, aq.valor FROM avaliacao_questoes aq "
                + "JOIN questoes q ON q.id = aq.questao_id WHERE aq.avaliacao_id = ? "
                + "ORDER BY aq.ordem NULLS LAST, q.id";
        try (PreparedStatement ps = c.prepareStatement(sqlQuestoes)) {
            ps.setInt(1, idAvaliacao);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    linhas.add(new Linha(rs.getInt("id"), tipoDoBanco(rs.getString("tipo")), rs.getBigDecimal("valor")));
                }
            }
        }

        Map<Integer, List<Alternativa>> alternativas = new LinkedHashMap<>();
        String sqlAlternativas = "SELECT a.id, a.questao_id, a.texto, a.correta, a.par_associacao FROM alternativas a "
                + "WHERE a.questao_id IN (SELECT questao_id FROM avaliacao_questoes WHERE avaliacao_id = ?) ORDER BY a.id";
        try (PreparedStatement ps = c.prepareStatement(sqlAlternativas)) {
            ps.setInt(1, idAvaliacao);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alternativas.computeIfAbsent(rs.getInt("questao_id"), k -> new ArrayList<>())
                            .add(new Alternativa(rs.getInt("id"), rs.getString("texto"),
                                    rs.getBoolean("correta"), rs.getString("par_associacao")));
                }
            }
        }

        Map<Integer, BigDecimal> valores = valoresDasQuestoes(linhas);

        List<QuestaoCorrecao> questoes = new ArrayList<>();
        for (Linha linha : linhas) {
            questoes.add(montar(linha, valores.get(linha.id()), alternativas.getOrDefault(linha.id(), List.of())));
        }
        return questoes;
    }

    /** Valores do professor; se alguma questão não tem valor, divide 10 igualmente. */
    private static Map<Integer, BigDecimal> valoresDasQuestoes(List<Linha> linhas) {
        boolean todosDefinidos = !linhas.isEmpty() && linhas.stream().allMatch(l -> l.valor() != null);
        if (todosDefinidos) {
            Map<Integer, BigDecimal> valores = new LinkedHashMap<>();
            linhas.forEach(l -> valores.put(l.id(), l.valor()));
            return valores;
        }
        return ValoresDaAvaliacao.dividirIgualmente(linhas.stream().map(Linha::id).toList());
    }

    private static QuestaoCorrecao montar(Linha linha, BigDecimal valor, List<Alternativa> alternativas) {
        switch (linha.tipo()) {
            case MULTIPLA_ESCOLHA, VERDADEIRO_FALSO -> {
                String idCerta = alternativas.stream().filter(Alternativa::correta).findFirst()
                        .map(a -> String.valueOf(a.id())).orElse(null);
                Map<String, String> textos = new LinkedHashMap<>();
                alternativas.forEach(a -> textos.put(String.valueOf(a.id()), a.texto()));
                return new QuestaoCorrecao(linha.id(), linha.tipo(), valor, idCerta, null, null, textos);
            }
            case ASSOCIACAO -> {
                Map<String, String> pares = new LinkedHashMap<>();
                alternativas.stream().filter(a -> a.par() != null)
                        .forEach(a -> pares.put(a.texto(), a.par()));
                return QuestaoCorrecao.associacao(linha.id(), valor, pares);
            }
            default -> {
                String esperada = alternativas.stream().filter(Alternativa::correta).findFirst()
                        .or(() -> alternativas.stream().findFirst())
                        .map(Alternativa::texto).orElse(null);
                return QuestaoCorrecao.discursiva(linha.id(), valor, esperada);
            }
        }
    }

    static TipoQuestao tipoDoBanco(String tipo) {
        return switch (tipo) {
            case "multipla_escolha" -> TipoQuestao.MULTIPLA_ESCOLHA;
            case "verdadeiro_falso" -> TipoQuestao.VERDADEIRO_FALSO;
            case "associacao" -> TipoQuestao.ASSOCIACAO;
            case "discursiva" -> TipoQuestao.DISCURSIVA;
            default -> throw new IllegalStateException("Tipo de questão desconhecido no banco: " + tipo);
        };
    }
}
