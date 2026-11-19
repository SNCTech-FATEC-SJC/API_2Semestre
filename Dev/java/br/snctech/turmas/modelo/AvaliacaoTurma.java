package br.snctech.turmas.modelo;

/**
 * Situação de uma avaliação dentro de uma turma: quantos alunos matriculados já enviaram a prova.
 */
public record AvaliacaoTurma(int avaliacaoId, String titulo, int concluiram, int totalAlunos) {

    public boolean concluidaPelaTurma() {
        return totalAlunos > 0 && concluiram >= totalAlunos;
    }

    public String situacao() {
        if (totalAlunos == 0) {
            return "Turma sem alunos";
        }
        return concluidaPelaTurma()
                ? "Concluída pela turma"
                : "Pendente (faltam " + (totalAlunos - concluiram) + ")";
    }
}
