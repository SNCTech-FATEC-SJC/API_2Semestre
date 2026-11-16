package br.snctech.correcao.service;

import br.snctech.correcao.corretor.CorretorQuestao;
import br.snctech.correcao.model.*;
import br.snctech.correcao.repositorio.RepositorioAvaliacoesRespondidas;
import br.snctech.correcao.repositorio.RepositorioQuestoesCorrecao;

import java.util.List;

public class CorrecaoAutomaticaService {

    private final List<CorretorQuestao> corretores;
    private final RepositorioAvaliacoesRespondidas repoRespondidas;
    private final RepositorioQuestoesCorrecao repoQuestoes;

    public CorrecaoAutomaticaService(List<CorretorQuestao> corretores,
                                     RepositorioAvaliacoesRespondidas repoRespondidas,
                                     RepositorioQuestoesCorrecao repoQuestoes) {
        this.corretores = corretores;
        this.repoRespondidas = repoRespondidas;
        this.repoQuestoes = repoQuestoes;
    }

    /** Ponto de entrada para o PB-07: chamar quando o aluno envia a prova. */
    public AvaliacaoRespondida corrigir(Integer idAvaliacaoRespondida) {
        AvaliacaoRespondida ar = repoRespondidas.buscar(idAvaliacaoRespondida);
        List<QuestaoCorrecao> questoes = repoQuestoes.buscarPorAvaliacao(ar.getIdAvaliacao());
        corrigir(ar, questoes);
        repoRespondidas.salvar(ar);
        return ar;
    }

    /**
     * Corrige as provas entregues da avaliação que ainda não foram corrigidas (alguma questão sem status).
     * Provas já corrigidas não são tocadas, e notas lançadas pelo professor nunca são sobrescritas.
     *
     * @return quantas provas foram corrigidas agora
     */
    public int corrigirNaoCorrigidas(Integer idAvaliacao) {
        List<QuestaoCorrecao> questoes = repoQuestoes.buscarPorAvaliacao(idAvaliacao);
        int corrigidas = 0;
        for (AvaliacaoRespondida ar : repoRespondidas.buscarPorAvaliacao(idAvaliacao)) {
            boolean faltaCorrigir = questoes.stream()
                    .anyMatch(q -> ar.resposta(q.id()).map(r -> r.getStatus() == null).orElse(true));
            if (faltaCorrigir) {
                corrigir(ar, questoes);
                repoRespondidas.salvar(ar);
                corrigidas++;
            }
        }
        return corrigidas;
    }

    /** Lógica pura (sem banco), fácil de testar. */
    public void corrigir(AvaliacaoRespondida ar, List<QuestaoCorrecao> questoes) {
        for (QuestaoCorrecao questao : questoes) {
            RespostaQuestao resposta = ar.respostaOuVazia(questao.id());
            if (resposta.getStatus() == StatusCorrecao.CORRIGIDA_MANUALMENTE) {
                continue; // nunca sobrescrever a nota do professor
            }
            CorretorQuestao corretor = corretores.stream()
                    .filter(c -> c.suporta(questao.tipo()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Nenhum corretor para o tipo " + questao.tipo()));
            resposta.aplicar(corretor.corrigir(questao, resposta));
        }
    }
}
