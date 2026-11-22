package br.snctech.notas.service;

import br.snctech.notas.model.MediaFinal;
import br.snctech.notas.model.NotaPonderada;
import br.snctech.notas.model.SituacaoFinal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CalculoMediaService {

    public static final BigDecimal MEDIA_APROVACAO = new BigDecimal("6.00");
    public static final BigDecimal MEDIA_EXAME_FINAL = new BigDecimal("4.00");

    public MediaFinal calcular(int idAluno, String nomeAluno, List<NotaPonderada> notas) {
        return calcular(idAluno, nomeAluno, notas, false);
    }

    public MediaFinal calcular(int idAluno, String nomeAluno, List<NotaPonderada> notas,
                               boolean turmaDeRecuperacao) {
        List<String> pendencias = new ArrayList<>();
        if (notas == null || notas.isEmpty()) {
            pendencias.add("Nenhuma avaliação desta turma entra na média ainda.");
            return new MediaFinal(idAluno, nomeAluno, null, SituacaoFinal.PENDENTE, pendencias);
        }

        BigDecimal somaNotasVezesPeso = BigDecimal.ZERO;
        BigDecimal somaPesos = BigDecimal.ZERO;
        for (NotaPonderada n : notas) {
            BigDecimal nota = notaQueEntraNaMedia(n, pendencias);
            if (nota != null) {
                somaNotasVezesPeso = somaNotasVezesPeso.add(nota.multiply(n.peso()));
            }
            somaPesos = somaPesos.add(n.peso());
        }

        if (!pendencias.isEmpty()) {
            return new MediaFinal(idAluno, nomeAluno, null, SituacaoFinal.PENDENTE, pendencias);
        }
        BigDecimal media = somaNotasVezesPeso.divide(somaPesos, 2, RoundingMode.HALF_UP);
        return new MediaFinal(idAluno, nomeAluno, media, situacaoPara(media, turmaDeRecuperacao), pendencias);
    }

    public SituacaoFinal situacaoPara(BigDecimal media) {
        if (media.compareTo(MEDIA_APROVACAO) >= 0) return SituacaoFinal.APROVADO;
        if (media.compareTo(MEDIA_EXAME_FINAL) >= 0) return SituacaoFinal.EXAME_FINAL;
        return SituacaoFinal.REPROVADO;
    }

    public SituacaoFinal situacaoPara(BigDecimal media, boolean turmaDeRecuperacao) {
        if (turmaDeRecuperacao && media.compareTo(MEDIA_APROVACAO) < 0) {
            return SituacaoFinal.REPROVADO;
        }
        return situacaoPara(media);
    }

    public boolean estaNaFaixaDeRecuperacao(BigDecimal media) {
        if (media == null) {
            return false;
        }
        BigDecimal mediaFinal = media.setScale(2, RoundingMode.HALF_UP);
        return mediaFinal.compareTo(MEDIA_EXAME_FINAL) > 0
                && mediaFinal.compareTo(MEDIA_APROVACAO) < 0;
    }

    public boolean podeLiberar(List<MediaFinal> mediasDaTurma) {
        return mediasDaTurma != null && !mediasDaTurma.isEmpty()
                && mediasDaTurma.stream().allMatch(MediaFinal::estaPronta);
    }

    private BigDecimal notaQueEntraNaMedia(NotaPonderada n, List<String> pendencias) {
        if (!n.avaliacaoEncerrada()) {
            pendencias.add(n.titulo() + ": o prazo ainda não terminou.");
            return null;
        }
        return switch (n.situacao()) {
            case NOTA -> n.nota();
            case NAO_REALIZADA -> BigDecimal.ZERO;
            case EM_CORRECAO -> {
                pendencias.add(n.titulo() + ": aguardando a correção do professor.");
                yield null;
            }
            case EM_ANDAMENTO -> {
                pendencias.add(n.titulo() + ": a prova ainda está em andamento.");
                yield null;
            }
        };
    }
}
