package br.snctech;

import java.util.ArrayList;
import java.util.List;

public class Questao {

    public static class ParColuna {
        private final String termoA;
        private final String termoB;

        public ParColuna(String termoA, String termoB) {
            this.termoA = termoA;
            this.termoB = termoB;
        }

        public String getTermoA() {
            return termoA;
        }

        public String getTermoB() {
            return termoB;
        }

        @Override
        public String toString() {
            return termoA + " → " + termoB;
        }
    }

    private String enunciado;
    private String tipo;
    private String imagemPath;
    private List<String> alternativas;
    private String respostaCorreta;
    private List<ParColuna> pares;

    public Questao(
            String enunciado,
            String tipo,
            String imagemPath,
            List<String> alternativas,
            String respostaCorreta,
            List<ParColuna> pares) {

        this.enunciado = enunciado;
        this.tipo = tipo;
        this.imagemPath = imagemPath;
        this.alternativas = alternativas == null
                ? new ArrayList<>()
                : new ArrayList<>(alternativas);
        this.respostaCorreta = respostaCorreta;
        this.pares = pares == null
                ? new ArrayList<>()
                : new ArrayList<>(pares);
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getImagemPath() {
        return imagemPath;
    }

    public void setImagemPath(String imagemPath) {
        this.imagemPath = imagemPath;
    }

    public List<String> getAlternativas() {
        return new ArrayList<>(alternativas);
    }

    public void setAlternativas(List<String> alternativas) {
        this.alternativas = alternativas == null
                ? new ArrayList<>()
                : new ArrayList<>(alternativas);
    }

    public String getRespostaCorreta() {
        return respostaCorreta;
    }

    public void setRespostaCorreta(String respostaCorreta) {
        this.respostaCorreta = respostaCorreta;
    }

    public List<ParColuna> getPares() {
        return new ArrayList<>(pares);
    }

    public void setPares(List<ParColuna> pares) {
        this.pares = pares == null
                ? new ArrayList<>()
                : new ArrayList<>(pares);
    }
}
