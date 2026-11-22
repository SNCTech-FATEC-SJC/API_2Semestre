package br.snctech.notas.repositorio;

import br.snctech.notas.model.AlunoDaTurma;
import br.snctech.notas.model.AvaliacaoDaTurma;
import br.snctech.notas.model.MediaFinal;
import br.snctech.notas.model.MediaLiberada;
import br.snctech.notas.model.TentativaRegistrada;
import br.snctech.notas.model.TurmaResumo;

import java.util.List;

public interface RepositorioMedias {

    List<TurmaResumo> turmasDoProfessor(int idProfessor);

    List<TurmaResumo> turmasDoAluno(String email);

    List<AvaliacaoDaTurma> avaliacoesDaTurma(int idTurma);

    List<AlunoDaTurma> alunosDaTurma(int idTurma);

    List<TentativaRegistrada> tentativasDaTurma(int idTurma);

    boolean turmaConcluida(int idTurma);

    void liberar(int idTurma);
}
