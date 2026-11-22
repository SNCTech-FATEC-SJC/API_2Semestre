package br.snctech.notas.model;

import java.util.List;

public record QuadroDaTurma(TurmaResumo turma, List<AvaliacaoDaTurma> avaliacoes, List<LinhaDoQuadro> linhas,
                            boolean podeLiberar) {

    public QuadroDaTurma {
        avaliacoes = List.copyOf(avaliacoes);
        linhas = List.copyOf(linhas);
    }

    public boolean todasLiberadas() {
        return !linhas.isEmpty() && linhas.stream().allMatch(LinhaDoQuadro::liberada);
    }

    public List<String> pendencias() {
        return linhas.stream()
                .filter(l -> !l.media().estaPronta())
                .flatMap(l -> l.media().pendencias().stream().map(p -> l.aluno().nome() + " – " + p))
                .toList();
    }
}
