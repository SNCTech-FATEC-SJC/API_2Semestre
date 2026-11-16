package br.snctech.correcao.repositorio.memoria;

import br.snctech.correcao.model.AlunoResumo;
import br.snctech.correcao.repositorio.RepositorioAlunos;

import java.util.List;

/** Alunos de demonstração (a turma real virá do cadastro de turmas/alunos, PB-09/PB-10). */
public class RepositorioAlunosMemoria implements RepositorioAlunos {

    private final List<AlunoResumo> alunos = List.of(
            new AlunoResumo(1, "João Silva"),
            new AlunoResumo(2, "Maria Souza"),
            new AlunoResumo(3, "Pedro Santos"));

    @Override
    public List<AlunoResumo> buscarPorAvaliacao(Integer idAvaliacao) {
        return alunos;
    }
}
