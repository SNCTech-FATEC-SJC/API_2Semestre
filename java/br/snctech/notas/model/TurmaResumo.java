package br.snctech.notas.model;

public record TurmaResumo(int id, String nome) {

    @Override
    public String toString() {
        return nome;
    }

    public boolean ehRecuperacao() {
        return nome != null && nome.toLowerCase(java.util.Locale.ROOT).startsWith("recuperação turma #");
    }
}
