package br.snctech.Repositorio;

import br.snctech.BancoQuestoes;
import br.snctech.Questao;
import java.util.ArrayList;
import java.util.List;

/** Adaptador: avaliações consultam o banco de questões mantido pela outra dupla.
 * Não mantém uma segunda lista nem cria questões de exemplo separadas. */
public class RepositorioQuestoes {
    public List<Questao> listarTodas() {
        return new ArrayList<>(BancoQuestoes.getQuestoes());
    }
}
