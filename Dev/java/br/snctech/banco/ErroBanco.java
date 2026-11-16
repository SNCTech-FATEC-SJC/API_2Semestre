package br.snctech.banco;

import java.sql.SQLException;

/**
 * Falha ao acessar o banco de dados. É uma exceção "sem verificação" para que os repositórios
 * mantenham as mesmas assinaturas da versão em memória; as telas capturam e mostram a mensagem.
 */
public class ErroBanco extends RuntimeException {
    private final String sqlState;

    public ErroBanco(String mensagem) {
        super(mensagem);
        this.sqlState = null;
    }

    public ErroBanco(String mensagem, SQLException causa) {
        super(mensagem, causa);
        this.sqlState = causa == null ? null : causa.getSQLState();
    }

    /** SQLSTATE 23505 = violação de chave única (ex.: o aluno já tem tentativa nesta avaliação). */
    public boolean violacaoDeUnicidade() {
        return "23505".equals(sqlState);
    }
}
