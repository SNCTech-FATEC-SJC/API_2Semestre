package br.snctech.correcao.service;

import br.snctech.correcao.model.AlunoResumo;
import br.snctech.correcao.model.AvaliacaoRespondida;
import br.snctech.correcao.model.StatusCorrecao;
import br.snctech.correcao.repositorio.RepositorioAlunos;
import br.snctech.correcao.repositorio.RepositorioAvaliacoesRespondidas;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResumoCorrecaoService {
    public enum SituacaoAluno { NAO_RESPONDEU, PENDENTE, CORRIGIDA, FINALIZADA }

    public record LinhaAluno(Integer idAluno, String nome, SituacaoAluno situacao,
                             long discursivasPendentes, BigDecimal nota, Integer idAvaliacaoRespondida) {}

    public record ResumoAvaliacao(Integer idAvaliacao, int percentualCorrigido, List<LinhaAluno> alunos) {}

    private final RepositorioAvaliacoesRespondidas repoRespondidas;
    private final RepositorioAlunos repoAlunos;

    public ResumoCorrecaoService(RepositorioAvaliacoesRespondidas repoRespondidas, RepositorioAlunos repoAlunos) {
        this.repoRespondidas = repoRespondidas;
        this.repoAlunos = repoAlunos;
    }

    public ResumoAvaliacao resumir(Integer idAvaliacao, boolean prazoEncerrado) {
        Map<Integer, AvaliacaoRespondida> porAluno = repoRespondidas.buscarPorAvaliacao(idAvaliacao).stream()
                .collect(Collectors.toMap(AvaliacaoRespondida::getIdAluno, Function.identity(), (a, b) -> b));

        List<LinhaAluno> linhas = new ArrayList<>();
        for (AlunoResumo aluno : repoAlunos.buscarPorAvaliacao(idAvaliacao)) {
            AvaliacaoRespondida ar = porAluno.get(aluno.id());
            if (ar == null) {
                linhas.add(new LinhaAluno(aluno.id(), aluno.nome(), SituacaoAluno.NAO_RESPONDEU, 0,
                        prazoEncerrado ? BigDecimal.ZERO : null, null));
            } else if (ar.notaFinalConfirmada()) {
                SituacaoAluno situacao = ar.isCorrecaoFinalizada() ? SituacaoAluno.FINALIZADA : SituacaoAluno.CORRIGIDA;
                linhas.add(new LinhaAluno(aluno.id(), aluno.nome(), situacao, 0, ar.notaTotal(), ar.getId()));
            } else {
                long pendentes = ar.respostas().values().stream()
                        .filter(r -> r.getStatus() == null || r.getStatus() == StatusCorrecao.PENDENTE_MANUAL)
                        .count();
                linhas.add(new LinhaAluno(aluno.id(), aluno.nome(), SituacaoAluno.PENDENTE, pendentes,
                        null, ar.getId()));
            }
        }
        linhas.sort(Comparator.comparing(LinhaAluno::nome));

        long concluidos = linhas.stream()
                .filter(l -> l.situacao() == SituacaoAluno.CORRIGIDA || l.situacao() == SituacaoAluno.FINALIZADA
                        || (l.situacao() == SituacaoAluno.NAO_RESPONDEU && prazoEncerrado))
                .count();
        int percentual = linhas.isEmpty() ? 0 : (int) Math.round(100.0 * concluidos / linhas.size());

        return new ResumoAvaliacao(idAvaliacao, percentual, linhas);
    }
}
