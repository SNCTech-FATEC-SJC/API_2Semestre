package br.snctech.Repositorio;

import br.snctech.Questao;
import br.snctech.model.Avaliacao;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Onde as avaliações ficam guardadas. Há duas implementações: RepositorioAvaliacoesMemoria
 * (sem banco, usada nos testes e no modo memória) e RepositorioAvaliacoesJdbc (PostgreSQL).
 * As regras de negócio ficam no AvaliacaoService; aqui só se guarda e se lê.
 */
public interface RepositorioAvaliacoes {

    Avaliacao salvar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo);

    Avaliacao salvar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                     LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos);

    /** Nova versão de uma avaliação que já foi respondida: guarda o vínculo com a original (versão e avaliação-pai). */
    Avaliacao salvarNovaVersao(Avaliacao original, String nome, List<Questao> questoes, String turma,
                               LocalDate data, LocalDate prazo, LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos);

    List<Avaliacao> listarTodas();

    /** null se não existe. */
    Avaliacao buscarPorId(int id);

    /** Altera a avaliação no repositório e também o objeto recebido. */
    void atualizar(Avaliacao avaliacao, String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                   LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos);

    /** Disponibiliza (ou retira) a avaliação para os alunos. Altera o repositório e também o objeto recebido. */
    void atualizarDisponibilizacao(Avaliacao avaliacao, boolean disponibilizada);

    default boolean existeNome(String nome) {
        return listarTodas().stream().anyMatch(a -> a.getNome().equalsIgnoreCase(nome));
    }

    default boolean contemQuestao(int avaliacaoId, Questao questao) {
        Avaliacao avaliacao = buscarPorId(avaliacaoId);
        return avaliacao != null && avaliacao.getQuestoes().contains(questao);
    }
}
