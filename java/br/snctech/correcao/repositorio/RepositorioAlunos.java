package br.snctech.correcao.repositorio;

import br.snctech.correcao.model.AlunoResumo;

import java.util.List;

/** Alunos das turmas em que a avaliação foi aplicada, inclusive quem não respondeu (no banco: avaliacoes → turmas → matriculas → usuarios). */
public interface RepositorioAlunos {
    List<AlunoResumo> buscarPorAvaliacao(Integer idAvaliacao);
}
