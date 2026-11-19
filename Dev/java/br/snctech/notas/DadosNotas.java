package br.snctech.notas;

import br.snctech.banco.Conexao;
import br.snctech.correcao.DadosCorrecao;
import br.snctech.notas.repositorio.RepositorioMediasPostgres;
import br.snctech.notas.service.CalculoMediaService;
import br.snctech.notas.service.MediasTurmaService;

import java.time.LocalDateTime;

public final class DadosNotas {

    private static MediasTurmaService servico;

    private DadosNotas() {
    }

    public static synchronized MediasTurmaService servico() {
        if (servico == null) {
            servico = new MediasTurmaService(new RepositorioMediasPostgres(), new CalculoMediaService(),
                    LocalDateTime::now, id -> DadosCorrecao.correcaoAutomatica().corrigirNaoCorrigidas(id));
        }
        return servico;
    }

    public static synchronized void definirServico(MediasTurmaService novoServico) {
        servico = novoServico;
    }

    public static boolean disponivel() {
        return !Conexao.modoMemoria();
    }

    public static String verificarTabela() {
        if (!disponivel()) {
            return "As notas e médias da turma só funcionam com o banco de dados (modo=banco no banco.properties).";
        }
        try {
            boolean existe = Conexao.executar(RepositorioMediasPostgres::tabelaExiste);
            return existe ? null
                    : "Falta a tabela medias_finais no banco. Rode o arquivo DB_SQL/migracao_03_notas_e_medias.sql no pgAdmin.";
        } catch (RuntimeException erro) {
            return "Não foi possível acessar o banco de dados. Detalhe: " + erro.getMessage();
        }
    }

    public static void recalcularAposCorrecao(int idAvaliacao) {
        if (!disponivel() || verificarTabela() != null) {
            return;
        }
        try {
            servico().recalcularPelaAvaliacao(idAvaliacao);
        } catch (RuntimeException erro) {
            System.err.println("Não foi possível recalcular a média: " + erro.getMessage());
        }
    }
}
