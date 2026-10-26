package br.snctech.correcao.repositorio;

/**
 * Entrega o enunciado de uma questão para as telas de correção.
 * O enunciado pertence ao cadastro de questões (PB-02); a correção não o guarda.
 *
 * <p>Na integração, basta uma lambda que procure a questão no cadastro:
 * {@code id -> bancoQuestoes.buscarPorId(id).getEnunciado()}.
 */
@FunctionalInterface
public interface FonteDeEnunciados {
    String enunciado(int idQuestao);
}
