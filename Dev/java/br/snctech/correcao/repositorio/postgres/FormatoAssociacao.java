package br.snctech.correcao.repositorio.postgres;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Formato em que a resposta de "Ligar Colunas" é guardada no banco (coluna respostas_aluno.resposta_associacao).
 * É o mesmo formato que a tela do aluno grava: {@code "termoA → termoB; termoC → termoD"}.
 */
public final class FormatoAssociacao {

    private FormatoAssociacao() {
    }

    /** "A → B; C → D" vira {A=B, C=D}. Aceita "->" no lugar de "→". Texto vazio vira mapa vazio. */
    public static Map<String, String> ler(String texto) {
        Map<String, String> pares = new LinkedHashMap<>();
        if (texto == null || texto.isBlank()) {
            return pares;
        }
        for (String par : texto.split(";")) {
            String[] lados = par.split("\\s*(?:→|->)\\s*", 2);
            if (lados.length == 2 && !lados[0].isBlank() && !lados[1].isBlank()) {
                pares.put(lados[0].trim(), lados[1].trim());
            }
        }
        return pares;
    }

    /** {A=B, C=D} vira "A → B; C → D". */
    public static String formatar(Map<String, String> pares) {
        StringBuilder texto = new StringBuilder();
        for (Map.Entry<String, String> par : pares.entrySet()) {
            if (texto.length() > 0) {
                texto.append("; ");
            }
            texto.append(par.getKey()).append(" → ").append(par.getValue());
        }
        return texto.toString();
    }
}
