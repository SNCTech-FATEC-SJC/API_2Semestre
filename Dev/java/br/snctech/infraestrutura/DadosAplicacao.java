package br.snctech.infraestrutura;

import br.snctech.avaliacoes.repositorio.RepositorioAvaliacoes;
import br.snctech.avaliacoes.repositorio.RepositorioAvaliacoesMemoria;
import br.snctech.questoes.repositorio.RepositorioQuestoes;
import br.snctech.avaliacoes.repositorio.RepositorioTentativas;
import br.snctech.avaliacoes.repositorio.RepositorioTentativasMemoria;
import br.snctech.avaliacoes.repositorio.RepositorioAvaliacoesJdbc;
import br.snctech.avaliacoes.repositorio.RepositorioTentativasJdbc;
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
