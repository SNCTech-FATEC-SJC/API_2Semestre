package br.snctech.correcao.controller;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.Locale;

/** Pequenas peças reaproveitadas pelas telas de correção (selo colorido, formatação, alerta). */
final class PecasVisuais {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    enum Cor { VERDE, LARANJA, VERMELHO, AZUL, CINZA }

    private PecasVisuais() {
    }

    static Label selo(String texto, Cor cor) {
        Label l = new Label(texto);
        l.getStyleClass().addAll("corr-selo", "corr-selo-" + cor.name().toLowerCase());
        return l;
    }

    static VBox cartao(double espacamento) {
        VBox v = new VBox(espacamento);
        v.getStyleClass().add("corr-cartao");
        v.setPadding(new Insets(14));
        return v;
    }

    static VBox secao(String titulo, String texto) {
        Label t = new Label(titulo);
        t.getStyleClass().add("corr-secao-titulo");
        Label corpo = new Label(texto);
        corpo.setWrapText(true);
        return new VBox(2, t, corpo);
    }

    /** 2 casas com vírgula (1,50); null vira "–". */
    static String formatar(BigDecimal valor) {
        return valor == null ? "–" : String.format(PT_BR, "%.2f", valor);
    }

    /** Aceita "1,5" e "1.5". Devolve null se não for número. */
    static BigDecimal lerNota(String texto) {
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    static void alerta(String mensagem) {
        new Alert(Alert.AlertType.WARNING, mensagem == null ? "Operação inválida." : mensagem).showAndWait();
    }
}
