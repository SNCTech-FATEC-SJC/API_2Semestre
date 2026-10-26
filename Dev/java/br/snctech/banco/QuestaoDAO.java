package br.snctech.banco;

import br.snctech.Questao;
import br.snctech.Questao.ParColuna;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Leitura e gravação de questões (tabelas questoes e alternativas).
 *
 * Como cada tipo de questão vira linhas (convenção desta camada; se o grupo do banco de questões
 * definir outra, é só ajustar aqui):
 *   Múltipla Escolha  -> multipla_escolha: uma linha em alternativas por opção; correta = a opção certa.
 *   Verdadeiro/Falso  -> verdadeiro_falso: duas linhas ("Verdadeiro" e "Falso"); correta = a resposta certa.
 *   Dissertativa      -> discursiva: uma linha com a resposta esperada (correta = true).
 *   Ligar Colunas     -> associacao: uma linha por par; texto = termo A, par_associacao = termo B.
 */
public final class QuestaoDAO {
    private QuestaoDAO() { }

    private static final String SELECT_QUESTOES = "SELECT q.id, q.tipo, q.enunciado, q.imagem_url FROM questoes q ";
    private static final String SELECT_ALTERNATIVAS =
            "SELECT a.questao_id, a.texto, a.correta, a.par_associacao FROM alternativas a ";

    private static final String NO_PROFESSOR = "WHERE q.professor_id = ?";
    private static final String NAS_AVALIACOES =
            "WHERE q.id IN (SELECT aq.questao_id FROM avaliacao_questoes aq)";
    private static final String NA_AVALIACAO =
            "WHERE q.id IN (SELECT aq.questao_id FROM avaliacao_questoes aq WHERE aq.avaliacao_id = ?)";
    private static final String NA_TENTATIVA =
            "WHERE q.id IN (SELECT ra.questao_id FROM respostas_aluno ra WHERE ra.tentativa_id = ?)";

    // ---------------------------------------------------------------- leitura

    public static Map<Integer, Questao> carregarDoProfessor(Connection c, int professorId) throws SQLException {
        return carregar(c, NO_PROFESSOR, professorId);
    }

    public static Map<Integer, Questao> carregarDeTodasAsAvaliacoes(Connection c) throws SQLException {
        return carregar(c, NAS_AVALIACOES, null);
    }

    public static Map<Integer, Questao> carregarDeAvaliacao(Connection c, int avaliacaoId) throws SQLException {
        return carregar(c, NA_AVALIACAO, avaliacaoId);
    }

    public static Map<Integer, Questao> carregarDeTentativa(Connection c, int tentativaId) throws SQLException {
        return carregar(c, NA_TENTATIVA, tentativaId);
    }

    private record Linha(int id, String tipo, String enunciado, String imagem) { }

    private record Alternativa(String texto, boolean correta, String par) { }

    private static Map<Integer, Questao> carregar(Connection c, String filtro, Integer parametro) throws SQLException {
        List<Linha> linhas = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(SELECT_QUESTOES + filtro + " ORDER BY q.id")) {
            if (parametro != null) ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    linhas.add(new Linha(rs.getInt("id"), rs.getString("tipo"), rs.getString("enunciado"), rs.getString("imagem_url")));
                }
            }
        }

        Map<Integer, List<Alternativa>> alternativas = new LinkedHashMap<>();
        String sqlAlternativas = SELECT_ALTERNATIVAS + "WHERE a.questao_id IN (SELECT q.id FROM questoes q " + filtro
                + ") ORDER BY a.id";
        try (PreparedStatement ps = c.prepareStatement(sqlAlternativas)) {
            if (parametro != null) ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alternativas.computeIfAbsent(rs.getInt("questao_id"), k -> new ArrayList<>())
                            .add(new Alternativa(rs.getString("texto"), rs.getBoolean("correta"), rs.getString("par_associacao")));
                }
            }
        }

        Map<Integer, Questao> resultado = new LinkedHashMap<>();
        for (Linha linha : linhas) {
            resultado.put(linha.id(), montar(linha, alternativas.getOrDefault(linha.id(), List.of())));
        }
        return resultado;
    }

    private static Questao montar(Linha linha, List<Alternativa> alternativas) {
        List<String> opcoes = new ArrayList<>();
        List<ParColuna> pares = new ArrayList<>();
        String respostaCorreta = null;

        switch (linha.tipo()) {
            case "multipla_escolha" -> {
                for (Alternativa a : alternativas) {
                    opcoes.add(a.texto());
                    if (a.correta() && respostaCorreta == null) respostaCorreta = a.texto();
                }
            }
            case "verdadeiro_falso" -> {
                for (Alternativa a : alternativas) {
                    if (a.correta() && respostaCorreta == null) respostaCorreta = a.texto();
                }
            }
            case "associacao" -> {
                StringBuilder gabarito = new StringBuilder();
                for (Alternativa a : alternativas) {
                    pares.add(new ParColuna(a.texto(), a.par()));
                    if (gabarito.length() > 0) gabarito.append("; ");
                    gabarito.append(a.texto()).append(" → ").append(a.par());
                }
                respostaCorreta = gabarito.toString();
            }
            default -> { // discursiva
                if (!alternativas.isEmpty()) respostaCorreta = alternativas.get(0).texto();
            }
        }

        Questao questao = new Questao(linha.enunciado(), tipoDoBanco(linha.tipo()), linha.imagem(), opcoes, respostaCorreta, pares);
        questao.setId(linha.id());
        return questao;
    }

    // ---------------------------------------------------------------- gravação

    /** Grava uma questão nova (com suas alternativas) e devolve o id. Quem chama é dono da transação. */
    public static int inserir(Connection c, Questao questao, int professorId) throws SQLException {
        String sql = "INSERT INTO questoes (professor_id, tipo, enunciado, imagem_url) "
                + "VALUES (?, CAST(? AS tipo_questao), ?, ?) RETURNING id";
        int id;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, professorId);
            ps.setString(2, tipoParaBanco(questao.getTipo()));
            ps.setString(3, questao.getEnunciado());
            ps.setString(4, questao.getImagemPath());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                id = rs.getInt(1);
            }
        }

        String insertAlternativa = "INSERT INTO alternativas (questao_id, texto, correta, par_associacao) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(insertAlternativa)) {
            switch (questao.getTipo()) {
                case "Múltipla Escolha" -> {
                    for (String opcao : questao.getAlternativas()) {
                        gravarAlternativa(ps, id, opcao, opcao.equals(questao.getRespostaCorreta()), null);
                    }
                }
                case "Verdadeiro/Falso" -> {
                    for (String opcao : List.of("Verdadeiro", "Falso")) {
                        gravarAlternativa(ps, id, opcao, opcao.equals(questao.getRespostaCorreta()), null);
                    }
                }
                case "Ligar Colunas" -> {
                    for (ParColuna par : questao.getPares()) {
                        gravarAlternativa(ps, id, par.getTermoA(), true, par.getTermoB());
                    }
                }
                default -> { // Dissertativa: a resposta esperada
                    if (questao.getRespostaCorreta() != null && !questao.getRespostaCorreta().isBlank()) {
                        gravarAlternativa(ps, id, questao.getRespostaCorreta(), true, null);
                    }
                }
            }
        }
        return id;
    }

    private static void gravarAlternativa(PreparedStatement ps, int questaoId, String texto, boolean correta, String par)
            throws SQLException {
        ps.setInt(1, questaoId);
        ps.setString(2, texto);
        ps.setBoolean(3, correta);
        ps.setString(4, par);
        ps.executeUpdate();
    }

    /** O id da alternativa de uma questão com este texto (a de menor id, se houver repetidas); null se não existe. */
    public static Integer buscarIdDaAlternativa(Connection c, int questaoId, String texto) throws SQLException {
        String sql = "SELECT id FROM alternativas WHERE questao_id = ? AND texto = ? ORDER BY id LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, questaoId);
            ps.setString(2, texto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : null;
            }
        }
    }

    // ---------------------------------------------------------------- tipos

    public static String tipoParaBanco(String tipoDaTela) {
        if (tipoDaTela == null) throw new IllegalArgumentException("A questão não tem tipo definido.");
        switch (tipoDaTela) {
            case "Múltipla Escolha": return "multipla_escolha";
            case "Verdadeiro/Falso": return "verdadeiro_falso";
            case "Dissertativa": return "discursiva";
            case "Ligar Colunas": return "associacao";
            default: throw new IllegalArgumentException("Tipo de questão desconhecido: " + tipoDaTela);
        }
    }

    public static String tipoDoBanco(String tipoDoBanco) {
        switch (tipoDoBanco) {
            case "multipla_escolha": return "Múltipla Escolha";
            case "verdadeiro_falso": return "Verdadeiro/Falso";
            case "associacao": return "Ligar Colunas";
            default: return "Dissertativa";
        }
    }
}
