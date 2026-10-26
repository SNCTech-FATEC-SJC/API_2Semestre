package br.snctech;

import br.snctech.Repositorio.RepositorioAvaliacoes;
import br.snctech.Repositorio.RepositorioAvaliacoesMemoria;
import br.snctech.Repositorio.RepositorioQuestoes;
import br.snctech.Repositorio.RepositorioTentativas;
import br.snctech.Repositorio.RepositorioTentativasMemoria;
import br.snctech.Repositorio.RepositorioAvaliacoesJdbc;
import br.snctech.Repositorio.RepositorioTentativasJdbc;
import br.snctech.banco.Conexao;

/** Compartilha os repositórios enquanto a aplicação estiver aberta. */
public final class DadosAplicacao {
    private static final RepositorioAvaliacoes REPOSITORIO_AVALIACOES = Conexao.modoMemoria()
            ? new RepositorioAvaliacoesMemoria() : new RepositorioAvaliacoesJdbc();
    private static final RepositorioQuestoes REPOSITORIO_QUESTOES = new RepositorioQuestoes();
    private static final RepositorioTentativas REPOSITORIO_TENTATIVAS = Conexao.modoMemoria()
            ? new RepositorioTentativasMemoria() : new RepositorioTentativasJdbc();
    private DadosAplicacao() { }
    public static RepositorioAvaliacoes getRepositorioAvaliacoes() { return REPOSITORIO_AVALIACOES; }
    public static RepositorioQuestoes getRepositorioQuestoes() { return REPOSITORIO_QUESTOES; }
    public static RepositorioTentativas getRepositorioTentativas() { return REPOSITORIO_TENTATIVAS; }
}
