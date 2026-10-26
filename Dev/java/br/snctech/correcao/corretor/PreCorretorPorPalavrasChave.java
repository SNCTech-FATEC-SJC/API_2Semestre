package br.snctech.correcao.corretor;

import br.snctech.correcao.model.QuestaoCorrecao;
import br.snctech.correcao.model.RespostaQuestao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pré-correção simples: proporção das palavras relevantes (4+ letras, sem acento,
 * sem diferenciar maiúsculas) da resposta esperada que aparecem no texto do aluno.
 */
public class PreCorretorPorPalavrasChave implements PreCorretorDiscursiva {

    private static final int TAMANHO_MINIMO = 4;

    @Override
    public BigDecimal sugerirNota(QuestaoCorrecao questao, RespostaQuestao resposta) {
        Set<String> chaves = palavras(questao.respostaEsperada());
        if (chaves.isEmpty()) {
            return null;
        }
        Set<String> doAluno = palavras(resposta.getTexto());
        long achadas = chaves.stream().filter(doAluno::contains).count();
        return questao.valor()
                .multiply(BigDecimal.valueOf(achadas))
                .divide(BigDecimal.valueOf(chaves.size()), 2, RoundingMode.HALF_UP);
    }

    static Set<String> palavras(String texto) {
        if (texto == null || texto.isBlank()) {
            return Set.of();
        }
        String normalizado = Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return Arrays.stream(normalizado.split("[^a-z0-9]+"))
                .filter(p -> p.length() >= TAMANHO_MINIMO)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
