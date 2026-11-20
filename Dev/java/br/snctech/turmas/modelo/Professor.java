package br.snctech.turmas.modelo;

public record Professor(int id, String nome) {

    @Override
    public String toString() {
        return nome;
    }
}
