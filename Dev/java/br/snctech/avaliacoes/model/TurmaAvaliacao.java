package br.snctech.avaliacoes.model;

/**
 * Turma real (linha da tabela turmas) à qual a avaliação está vinculada.
 * O id é o que vale; o nome serve apenas para exibição.
 */
public record TurmaAvaliacao(int id, String nome) {
}
