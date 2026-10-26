package br.snctech;

import br.snctech.Repositorio.RepositorioAvaliacoes;
import br.snctech.Repositorio.RepositorioQuestoes;

/** Compartilha os repositórios enquanto a aplicação estiver aberta. */
public final class DadosAplicacao {
    private static final RepositorioAvaliacoes REPOSITORIO_AVALIACOES = new RepositorioAvaliacoes();
    private static final RepositorioQuestoes REPOSITORIO_QUESTOES = new RepositorioQuestoes();
    private DadosAplicacao() { }
    public static RepositorioAvaliacoes getRepositorioAvaliacoes() { return REPOSITORIO_AVALIACOES; }
    public static RepositorioQuestoes getRepositorioQuestoes() { return REPOSITORIO_QUESTOES; }
}
