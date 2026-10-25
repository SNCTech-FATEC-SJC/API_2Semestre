package br.snctech.model;

public class Questao {
    private int id;
    private String enunciado;
    private String gabarito;

    public Questao(int id, String enunciado, String gabarito) {
        this.id = id;
        this.enunciado = enunciado;
        this.gabarito = gabarito;
    }

    public int getId() {
        return id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String getGabarito() {
        return gabarito;
    }
}
