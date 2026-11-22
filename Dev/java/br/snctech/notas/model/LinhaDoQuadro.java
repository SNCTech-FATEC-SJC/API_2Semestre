package br.snctech.notas.model;

import br.snctech.correcao.model.NotaDoAluno;

import java.util.Map;

public record LinhaDoQuadro(AlunoDaTurma aluno, Map<Integer, NotaDoAluno> notasPorAvaliacao, MediaFinal media,
                            boolean liberada) {

    public LinhaDoQuadro {
        notasPorAvaliacao = Map.copyOf(notasPorAvaliacao);
    }

    public String textoDaNota(int idAvaliacao) {
        NotaDoAluno nota = notasPorAvaliacao.get(idAvaliacao);
        return nota == null ? "Aguardando prazo" : nota.textoDaNota();
    }

    public String textoDaSituacao() {
        return media.situacao().rotulo();
    }

    public String textoDaLiberacao() {
        if (!media.estaPronta()) {
            return "–";
        }
        return liberada ? "Liberada" : "Não liberada";
    }
}
