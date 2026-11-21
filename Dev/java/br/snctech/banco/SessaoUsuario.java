package br.snctech.banco;

import br.snctech.autenticacao.modelo.UsuarioLogado;

/**
 * Registro de quem está conectado. O login chama iniciar(usuario); sair da conta chama encerrar().
 *
 * getProfessorId() continua existindo para o código que já o usa: devolve o id do usuário logado.
 * Só sem ninguém logado (execução direta de uma tela em desenvolvimento) cai no professor de teste.
 */
public final class SessaoUsuario {
    private static UsuarioLogado usuario;
    private static Integer professorIdDeTeste;

    private SessaoUsuario() { }

    public static synchronized void iniciar(UsuarioLogado novoUsuario) {
        if (novoUsuario == null) {
            throw new IllegalArgumentException("Usuário da sessão não pode ser nulo.");
        }
        usuario = novoUsuario;
    }

    public static synchronized void encerrar() {
        usuario = null;
    }

    public static synchronized boolean logado() {
        return usuario != null;
    }

    /** Usuário logado, ou null se ninguém autenticou. */
    public static synchronized UsuarioLogado getUsuario() {
        return usuario;
    }

    public static synchronized int getProfessorId() {
        if (usuario != null) {
            return usuario.id();
        }
        if (professorIdDeTeste == null) {
            professorIdDeTeste = Conexao.executar(UsuarioDAO::buscarIdDoPrimeiroProfessor);
            if (professorIdDeTeste == null) {
                throw new ErroBanco("Não há nenhum professor cadastrado na tabela usuarios.");
            }
        }
        return professorIdDeTeste;
    }

    /** Apenas para testes sem login. */
    public static synchronized void definirProfessor(int id) {
        professorIdDeTeste = id;
    }
}
