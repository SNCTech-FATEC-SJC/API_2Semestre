package br.snctech.notas.model;

public record TurmaResumo(int id, String nome) {

    @Override
    public String toString() {
        return nome;
    }
}
