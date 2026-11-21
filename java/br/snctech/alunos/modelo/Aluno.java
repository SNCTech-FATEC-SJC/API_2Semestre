package br.snctech.alunos.modelo;

import java.time.LocalDateTime;

/** Aluno = usuário com perfil "aluno". turmaAtual pode ser null (sem turma ativa). */
public record Aluno(int id, String nome, String email, LocalDateTime criadoEm, String turmaAtual) {

    @Override
    public String toString() {
        return nome;
    }
}
