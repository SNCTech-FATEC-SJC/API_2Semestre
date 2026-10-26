package br.snctech.Repositorio;

import br.snctech.Questao;
import br.snctech.model.Tentativa;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Onde as tentativas dos alunos (e suas respostas) ficam guardadas. Implementações:
 * RepositorioTentativasMemoria e RepositorioTentativasJdbc (PostgreSQL).
 * O aluno é identificado pelo e-mail (enquanto não existe o login, PB-19).
 */
public interface RepositorioTentativas {

    Tentativa salvar(int avaliacaoId, String aluno, LocalDateTime inicio, LocalDateTime limite);

    /** null se não existe. */
    Tentativa buscarPorId(int id);

    /** A tentativa do aluno nesta avaliação, ou null se ele ainda não abriu a prova. */
    Tentativa buscarPorAvaliacaoEAluno(int avaliacaoId, String aluno);

    List<Tentativa> listarPorAvaliacao(int avaliacaoId);

    /** Guarda a resposta de uma questão (troca a anterior). Resposta vazia apaga a anterior. */
    void registrarResposta(Tentativa tentativa, Questao questao, String resposta);

    /** Muda o estado da tentativa (entregue/encerrada) e guarda o momento em que terminou. */
    void concluir(Tentativa tentativa, Tentativa.Status status, LocalDateTime momento);

    /** O aluno existe? Sem banco, qualquer identificação preenchida vale. */
    default boolean alunoExiste(String aluno) {
        return aluno != null && !aluno.isBlank();
    }

    default int contarPorAvaliacao(int avaliacaoId) {
        return listarPorAvaliacao(avaliacaoId).size();
    }
}
