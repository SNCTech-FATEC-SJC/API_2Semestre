package br.snctech.correcao.repositorio.postgres;

import br.snctech.banco.Conexao;
import br.snctech.banco.ErroBanco;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public final class EsquemaCorrecao {
    private EsquemaCorrecao() {
    }

    public static String verificar() {
        try {
            Conexao.executar(c -> {
                String[] consultas = {
                    "SELECT id, avaliacao_id, aluno_id, status, nota_total, corrigida_completamente, correcao_finalizada FROM tentativas_avaliacao WHERE 1 = 0",
                    "SELECT tentativa_id, questao_id, alternativa_escolhida_id, resposta_discursiva, resposta_associacao, "
                            + "nota_atribuida, validada, status_correcao, nota_sugerida, corrigido_por FROM respostas_aluno WHERE 1 = 0",
                    "SELECT avaliacao_id, questao_id, ordem, valor FROM avaliacao_questoes WHERE 1 = 0",
                    "SELECT id, turma_id, prazo_fim FROM avaliacoes WHERE 1 = 0",
                    "SELECT aluno_id, turma_id FROM matriculas WHERE 1 = 0"
                };
                try (Statement s = c.createStatement()) {
                    for (String consulta : consultas) {
                        try (ResultSet ignorado = s.executeQuery(consulta)) {
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT 1 FROM pg_indexes WHERE indexname = 'ux_resposta_tentativa_questao'");
                     ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ErroBanco("falta o índice ux_resposta_tentativa_questao (migração 01)");
                    }
                }
                return null;
            });
            return null;
        } catch (ErroBanco e) {
            return "O banco não está pronto para a Correção. Feche e abra o programa de novo para ele aplicar "
                    + "os scripts da pasta Avisos.e.Banco/DB_SQL. Detalhe: " + e.getMessage();
        }
    }
}
