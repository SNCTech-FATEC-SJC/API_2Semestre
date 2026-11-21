package br.snctech.avaliacoes.repositorio;


import br.snctech.avaliacoes.service.AvaliacaoService;
import br.snctech.questoes.model.Questao;
import br.snctech.avaliacoes.model.Avaliacao;
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

    default Avaliacao salvar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                             LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos, double peso) {
        Avaliacao avaliacao = salvar(nome, questoes, turma, data, prazo, horaInicio, horaFim, duracaoMinutos);
        avaliacao.setPeso(peso);
        return avaliacao;
    }

    /** Nova versão de uma avaliação que já foi respondida: guarda o vínculo com a original (versão e avaliação-pai). */
    Avaliacao salvarNovaVersao(Avaliacao original, String nome, List<Questao> questoes, String turma,
                               LocalDate data, LocalDate prazo, LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos);

    default Avaliacao salvarNovaVersao(Avaliacao original, String nome, List<Questao> questoes, String turma,
                                       LocalDate data, LocalDate prazo, LocalTime horaInicio, LocalTime horaFim,
                                       int duracaoMinutos, double peso) {
        Avaliacao avaliacao = salvarNovaVersao(original, nome, questoes, turma, data, prazo,
                horaInicio, horaFim, duracaoMinutos);
        avaliacao.setPeso(peso);
        return avaliacao;
    }

    List<Avaliacao> listarTodas();

    /** null se não existe. */
    Avaliacao buscarPorId(int id);

    /** Exclui a avaliação e seus dados dependentes do repositório. */
    void excluir(int id);

    /** Altera a avaliação no repositório e também o objeto recebido. */
    void atualizar(Avaliacao avaliacao, String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                   LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos);

    default void atualizar(Avaliacao avaliacao, String nome, List<Questao> questoes, String turma,
                           LocalDate data, LocalDate prazo, LocalTime horaInicio, LocalTime horaFim,
                           int duracaoMinutos, double peso) {
        atualizar(avaliacao, nome, questoes, turma, data, prazo, horaInicio, horaFim, duracaoMinutos);
        avaliacao.setPeso(peso);
    }

    /** Disponibiliza (ou retira) a avaliação para os alunos. Altera o repositório e também o objeto recebido. */
    void atualizarDisponibilizacao(Avaliacao avaliacao, boolean disponibilizada);

    default void configurarAlunos(Avaliacao avaliacao, List<Integer> alunosIds) {
        avaliacao.setAlunosSelecionados(alunosIds);
    }


    default boolean existeNome(String nome) {
        return listarTodas().stream().anyMatch(a -> a.getNome().equalsIgnoreCase(nome));
    }

    default boolean contemQuestao(int avaliacaoId, Questao questao) {
        Avaliacao avaliacao = buscarPorId(avaliacaoId);
        return avaliacao != null && avaliacao.getQuestoes().contains(questao);
    }
}
