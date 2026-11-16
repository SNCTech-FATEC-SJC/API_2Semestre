package br.snctech.correcao.repositorio;

import br.snctech.correcao.model.QuestaoCorrecao;

import java.util.List;

/** Devolve as questões de uma avaliação já com gabarito e valor (no banco: questoes + alternativas + avaliacao_questoes). */
public interface RepositorioQuestoesCorrecao {
    List<QuestaoCorrecao> buscarPorAvaliacao(Integer idAvaliacao);
}
