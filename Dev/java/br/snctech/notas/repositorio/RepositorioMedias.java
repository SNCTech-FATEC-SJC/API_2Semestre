package br.snctech.notas.repositorio;

import br.snctech.notas.model.AlunoDaTurma;
import br.snctech.notas.model.AvaliacaoDaTurma;
import br.snctech.notas.model.MediaFinal;
import br.snctech.notas.model.MediaLiberada;
import br.snctech.notas.model.TentativaRegistrada;
import br.snctech.notas.model.TurmaResumo;

import java.util.List;
import java.util.Map;

public interface RepositorioMedias {

    List<TurmaResumo> turmasDoProfessor(int idProfessor);

    List<AvaliacaoDaTurma> avaliacoesDaTurma(int idTurma);

    List<AlunoDaTurma> alunosDaTurma(int idTurma);

    List<TentativaRegistrada> tentativasDaTurma(int idTurma);

    void sincronizar(int idTurma, List<MediaFinal> mediasProntas);

    Map<Integer, Boolean> liberacaoPorAluno(int idTurma);

    void liberar(int idTurma);

    Integer turmaDaAvaliacao(int idAvaliacao);

    List<MediaLiberada> mediasLiberadasDoAluno(String email);
}
