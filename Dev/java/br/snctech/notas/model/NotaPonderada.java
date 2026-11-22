package br.snctech.notas.model;

import br.snctech.correcao.model.NotaDoAluno;

import java.math.BigDecimal;
import java.util.Objects;

public record NotaPonderada(int idAvaliacao, String titulo, BigDecimal peso, boolean avaliacaoEncerrada,
                            NotaDoAluno.Situacao situacao, BigDecimal nota) {
    public NotaPonderada {
        Objects.requireNonNull(situacao, "situacao");
        if (peso == null || peso.signum() <= 0) {
            throw new IllegalArgumentException("O peso da avaliação " + idAvaliacao + " precisa ser maior que zero.");
        }
        if (situacao == NotaDoAluno.Situacao.NOTA && nota == null) {
            throw new IllegalArgumentException("A avaliação " + idAvaliacao + " está corrigida, mas sem nota.");
        }
        if (nota != null && (nota.signum() < 0 || nota.compareTo(BigDecimal.TEN) > 0)) {
            throw new IllegalArgumentException("A nota da avaliação " + idAvaliacao + " precisa estar entre 0 e 10.");
        }
    }

    public static NotaPonderada de(NotaDoAluno nota, BigDecimal peso, boolean avaliacaoEncerrada) {
        return new NotaPonderada(nota.idAvaliacao(), nota.titulo(), peso, avaliacaoEncerrada,
                nota.situacao(), nota.nota());
    }
}
