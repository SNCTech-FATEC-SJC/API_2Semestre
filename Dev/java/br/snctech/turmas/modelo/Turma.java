package br.snctech.turmas.modelo;

import java.time.LocalDateTime;

/** Turma ativa = turma atual do professor; turma não ativa = turma concluída (histórico). */
public record Turma(int id, String nome, int professorId, String professorNome,
                    boolean ativa, LocalDateTime criadoEm, int totalAlunos) {

    public String situacao() {
        return ativa ? "Ativa" : "Concluída";
    }

    @Override
    public String toString() {
        return professorNome == null || professorNome.isBlank() ? nome : nome + " (" + professorNome + ")";
    }
}
