package br.snctech.banco;

/**
 * Quem está usando o sistema. Enquanto o login (PB-19) não existe, o professor é o primeiro
 * professor cadastrado no banco (Prof. Carlos, no dump de exemplo). Quando o login for feito,
 * é só chamar definirProfessor(id) depois de autenticar.
 */
public final class SessaoUsuario {
    private static Integer professorId;

    private SessaoUsuario() { }

    public static synchronized int getProfessorId() {
        if (professorId == null) {
            professorId = Conexao.executar(UsuarioDAO::buscarIdDoPrimeiroProfessor);
            if (professorId == null) {
                throw new ErroBanco("Não há nenhum professor cadastrado na tabela usuarios.");
            }
        }
        return professorId;
    }

    public static synchronized void definirProfessor(int id) {
        professorId = id;
    }
}
