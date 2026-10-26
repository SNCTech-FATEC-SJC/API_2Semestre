package br.snctech.correcao.model;

import java.text.Normalizer;

/**
 * Tipos de questão tratados pela correção.
 *
 * <p>O cadastro de questões do grupo (br.snctech.Questao) guarda o tipo como texto da tela:
 * "Múltipla Escolha", "Verdadeiro/Falso", "Dissertativa" e "Ligar Colunas".
 * {@link #deRotuloDoCadastro(String)} converte esse texto para o enum.
 */
public enum TipoQuestao {
    MULTIPLA_ESCOLHA,
    VERDADEIRO_FALSO,
    ASSOCIACAO,
    DISCURSIVA;

    /** Converte o texto usado na tela de cadastro (sem diferenciar acento nem maiúscula). */
    public static TipoQuestao deRotuloDoCadastro(String rotulo) {
        if (rotulo == null) {
            throw new IllegalArgumentException("Tipo de questão não informado.");
        }
        String r = Normalizer.normalize(rotulo.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return switch (r) {
            case "multipla escolha" -> MULTIPLA_ESCOLHA;
            case "verdadeiro/falso" -> VERDADEIRO_FALSO;
            case "ligar colunas" -> ASSOCIACAO;
            case "dissertativa" -> DISCURSIVA;
            default -> throw new IllegalArgumentException("Tipo de questão desconhecido: " + rotulo);
        };
    }
}
