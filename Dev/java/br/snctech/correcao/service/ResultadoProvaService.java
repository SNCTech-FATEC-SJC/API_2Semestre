package br.snctech.correcao.service;

import br.snctech.correcao.model.*;
import br.snctech.correcao.repositorio.RepositorioAvaliacoesRespondidas;
import br.snctech.correcao.repositorio.RepositorioQuestoesCorrecao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Dados da tela "Resultado da prova" (aluno) e da correção individual (professor). */
public class ResultadoProvaService {

    /** nota é null enquanto a questão aguarda o professor (o aluno não vê nota provisória). */
    public record ItemResultado(Integer idQuestao, TipoQuestao tipo, VereditoQuestao veredito,
                                BigDecimal nota, BigDecimal valor) {}

    /** notaFinal=false significa "Parcial"; true significa "Corrigida". */
    public record ResultadoProva(Integer idAvaliacaoRespondida, BigDecimal nota, boolean notaFinal,
                                 List<ItemResultado> itens) {}

    private final RepositorioAvaliacoesRespondidas repoRespondidas;
    private final RepositorioQuestoesCorrecao repoQuestoes;

    public ResultadoProvaService(RepositorioAvaliacoesRespondidas repoRespondidas, RepositorioQuestoesCorrecao repoQuestoes) {
        this.repoRespondidas = repoRespondidas;
        this.repoQuestoes = repoQuestoes;
    }

    public ResultadoProva resultadoDoAluno(Integer idAvaliacaoRespondida) {
        AvaliacaoRespondida ar = repoRespondidas.buscar(idAvaliacaoRespondida);
        List<ItemResultado> itens = new ArrayList<>();

        for (QuestaoCorrecao q : repoQuestoes.buscarPorAvaliacao(ar.getIdAvaliacao())) {
            RespostaQuestao r = ar.resposta(q.id()).orElseGet(() -> new RespostaQuestao(q.id()));
            VereditoQuestao veredito = VereditoQuestao.de(r, q.valor());
            BigDecimal nota = veredito == VereditoQuestao.AGUARDANDO_PROFESSOR ? null : r.getNota();
            itens.add(new ItemResultado(q.id(), q.tipo(), veredito, nota, q.valor()));
        }
        return new ResultadoProva(ar.getId(), ar.notaTotal(), ar.notaFinalConfirmada(), itens);
    }
}
