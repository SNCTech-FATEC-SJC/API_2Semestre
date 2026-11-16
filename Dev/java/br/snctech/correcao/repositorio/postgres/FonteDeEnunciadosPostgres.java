package br.snctech.correcao.repositorio.postgres;

import br.snctech.banco.Conexao;
import br.snctech.correcao.repositorio.FonteDeEnunciados;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/** Enunciados lidos da tabela questoes (o cadastro é de outra parte do projeto). Guarda em memória o que já leu. */
public class FonteDeEnunciadosPostgres implements FonteDeEnunciados {

    private final Map<Integer, String> cache = new HashMap<>();

    @Override
    public synchronized String enunciado(int idQuestao) {
        return cache.computeIfAbsent(idQuestao, id -> Conexao.executar(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT enunciado FROM questoes WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getString(1) : "(questão " + id + " não encontrada)";
                }
            }
        }));
    }
}
