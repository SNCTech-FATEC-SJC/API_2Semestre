package br.snctech.turmas.modelo;

import java.time.LocalDateTime;

/** Aluno matriculado em uma turma. */
public record Membro(int alunoId, String nome, String email, LocalDateTime matriculadoEm) {
}
