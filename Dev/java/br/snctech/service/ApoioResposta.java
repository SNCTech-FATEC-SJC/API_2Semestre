package br.snctech.service;

import br.snctech.Questao;
import java.util.List;

/** Pequenos formatadores usados pela tela Responder Avaliação (ficam aqui para poderem ser testados sem JavaFX). */
public final class ApoioResposta {
    private ApoioResposta() { }

    /** Segundos em HH:MM:SS (as horas podem passar de 99). Valores negativos viram 00:00:00. */
    public static String formatarTempo(long segundos) {
        if (segundos < 0) segundos = 0;
        return String.format("%02d:%02d:%02d", segundos / 3600, (segundos % 3600) / 60, segundos % 60);
    }

    /**
     * Resposta de "Ligar Colunas" no mesmo formato do gabarito: "A → B; C → D".
     * escolhas[i] é o termo B escolhido para o par i; pares sem escolha ficam de fora.
     * Devolve "" se nada foi escolhido (o que apaga a resposta da questão).
     */
    public static String montarRespostaColunas(List<Questao.ParColuna> pares, List<String> escolhas) {
        StringBuilder resposta = new StringBuilder();
        for (int i = 0; i < pares.size() && i < escolhas.size(); i++) {
            String escolha = escolhas.get(i);
            if (escolha == null || escolha.isBlank()) continue;
            if (resposta.length() > 0) resposta.append("; ");
            resposta.append(pares.get(i).getTermoA()).append(" → ").append(escolha);
        }
        return resposta.toString();
    }
}
