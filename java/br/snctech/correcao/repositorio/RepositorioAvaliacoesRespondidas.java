package br.snctech.correcao.repositorio;

import br.snctech.correcao.model.AvaliacaoRespondida;

import java.util.List;

/**
 * Porta de acesso às provas respondidas. É o que o responsável pelo banco (PostgreSQL) implementa.
 *
 * <p>Correspondência com o banco:
 * <ul>
 *   <li>{@code AvaliacaoRespondida}  = linha de {@code tentativas_avaliacao}</li>
 *   <li>{@code RespostaQuestao}      = linha de {@code respostas_aluno} (ligada pela tentativa)</li>
 *   <li>{@code notaTotal()}          = {@code tentativas_avaliacao.nota_total}</li>
 *   <li>{@code notaFinalConfirmada()} = {@code tentativas_avaliacao.corrigida_completamente}</li>
 * </ul>
 * Detalhes e colunas a combinar: veja README_CORRECAO.md.
 */
public interface RepositorioAvaliacoesRespondidas {

    /** @throws IllegalArgumentException se não existir prova respondida com esse id (nunca devolve null) */
    AvaliacaoRespondida buscar(Integer id);

    /** Provas já respondidas de uma avaliação (alunos que não responderam não aparecem aqui). */
    List<AvaliacaoRespondida> buscarPorAvaliacao(Integer idAvaliacao);

    /** Grava respostas, notas e status; no banco, também {@code nota_total} e {@code corrigida_completamente}. */
    void salvar(AvaliacaoRespondida avaliacaoRespondida);
}
