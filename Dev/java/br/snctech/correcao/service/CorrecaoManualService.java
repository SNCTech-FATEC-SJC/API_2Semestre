package br.snctech.correcao.service;

import br.snctech.correcao.model.*;
import br.snctech.correcao.repositorio.RepositorioAvaliacoesRespondidas;
import br.snctech.correcao.repositorio.RepositorioQuestoesCorrecao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CorrecaoManualService {

    private final RepositorioAvaliacoesRespondidas repoRespondidas;
    private final RepositorioQuestoesCorrecao repoQuestoes;

    public CorrecaoManualService(RepositorioAvaliacoesRespondidas repoRespondidas,
                                 RepositorioQuestoesCorrecao repoQuestoes) {
        this.repoRespondidas = repoRespondidas;
        this.repoQuestoes = repoQuestoes;
    }

    /** Discursivas aguardando o professor, de todos os alunos de uma avaliação. */
    public List<PendenciaDiscursiva> listarPendentes(Integer idAvaliacao) {
        List<QuestaoCorrecao> questoes = repoQuestoes.buscarPorAvaliacao(idAvaliacao);
        List<PendenciaDiscursiva> pendencias = new ArrayList<>();

        for (AvaliacaoRespondida ar : repoRespondidas.buscarPorAvaliacao(idAvaliacao)) {
            for (RespostaQuestao r : ar.respostas().values()) {
                if (r.getStatus() == StatusCorrecao.PENDENTE_MANUAL) {
                    QuestaoCorrecao q = questoes.stream()
                            .filter(x -> x.id().equals(r.getIdQuestao()))
                            .findFirst().orElse(null);
                    pendencias.add(new PendenciaDiscursiva(
                            ar.getId(), ar.getIdAluno(), r.getIdQuestao(), r.getTexto(),
                            q == null ? null : q.respostaEsperada(),
                            r.getNotaSugerida(),
                            q == null ? BigDecimal.ZERO : q.valor()));
                }
            }
        }
        return pendencias;
    }

    /** Professor aceita a nota sugerida pelo sistema ("Validar nota"). */
    public AvaliacaoRespondida validarSugestao(Integer idAvaliacaoRespondida, Integer idQuestao, Integer idProfessor) {
        AvaliacaoRespondida ar = repoRespondidas.buscar(idAvaliacaoRespondida);
        BigDecimal sugerida = ar.resposta(idQuestao)
                .orElseThrow(() -> new IllegalArgumentException("Questão não respondida: " + idQuestao))
                .getNotaSugerida();
        if (sugerida == null) {
            throw new IllegalStateException("A questão " + idQuestao + " não tem nota sugerida.");
        }
        return lancarNota(idAvaliacaoRespondida, idQuestao, sugerida, idProfessor);
    }

    /**
     * Professor lança ou ALTERA a nota de qualquer questão da prova (0 até o valor da questão):
     * discursiva pendente, discursiva já corrigida ou até questão objetiva que o sistema corrigiu
     * (DoD do PB-03 e do PB-08: "o professor pode alterar uma correção já realizada").
     *
     * <p>A questão passa a {@code CORRIGIDA_MANUALMENTE}; por isso a correção automática
     * nunca mais sobrescreve essa nota.
     * Depois de chamar, use {@code resultado.notaFinalConfirmada()} para saber se a prova foi fechada.
     */
    public AvaliacaoRespondida lancarNota(Integer idAvaliacaoRespondida, Integer idQuestao,
                                          BigDecimal nota, Integer idProfessor) {
        AvaliacaoRespondida ar = repoRespondidas.buscar(idAvaliacaoRespondida);

        QuestaoCorrecao questao = repoQuestoes.buscarPorAvaliacao(ar.getIdAvaliacao()).stream()
                .filter(q -> q.id().equals(idQuestao))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Questão fora da avaliação: " + idQuestao));

        if (nota == null || nota.signum() < 0 || nota.compareTo(questao.valor()) > 0) {
            throw new IllegalArgumentException(
                    "A nota deve estar entre 0 e " + questao.valor());
        }

        // Questão em branco (aluno não respondeu) também pode receber nota do professor.
        RespostaQuestao resposta = ar.respostaOuVazia(idQuestao);
        resposta.setNota(nota);
        resposta.setStatus(StatusCorrecao.CORRIGIDA_MANUALMENTE);
        resposta.setCorrigidoPor(idProfessor);

        repoRespondidas.salvar(ar);
        return ar;
    }
}
