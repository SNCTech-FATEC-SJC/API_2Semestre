package br.snctech.banco;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Consultas à tabela usuarios. */
public final class UsuarioDAO {
    private UsuarioDAO() { }

    public record Usuario(int id, String nome, String email) { }

    /** Dados usados apenas na autenticação. perfil vem como texto do enum (professor, aluno, admin). */
    public record Credencial(int id, String nome, String email, String senha, String perfil) { }

    /** Provisório (execução sem login): o primeiro professor cadastrado. null se não há nenhum. */
    public static Integer buscarIdDoPrimeiroProfessor(Connection c) throws SQLException {
        String sql = "SELECT id FROM usuarios WHERE perfil = CAST(? AS perfil_usuario) ORDER BY id LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "professor");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : null;
            }
        }
    }

    /** Só devolve usuários com perfil de aluno. null se o e-mail não existe. */
    public static Usuario buscarAlunoPorEmail(Connection c, String email) throws SQLException {
        String sql = "SELECT id, nome, email FROM usuarios "
                + "WHERE perfil = CAST(? AS perfil_usuario) AND lower(email) = lower(?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "aluno");
            ps.setString(2, email == null ? "" : email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("email")) : null;
            }
        }
    }

    /** Usuário de qualquer perfil pelo e-mail (sem diferenciar maiúsculas). null se não existe. */
    public static Credencial buscarCredencialPorEmail(Connection c, String email) throws SQLException {
        String sql = "SELECT id, nome, email, senha_hash, perfil::text AS perfil FROM usuarios "
                + "WHERE lower(email) = lower(?) ORDER BY id LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email == null ? "" : email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? new Credencial(rs.getInt("id"), rs.getString("nome"), rs.getString("email"),
                                rs.getString("senha_hash"), rs.getString("perfil"))
                        : null;
            }
        }
    }
}
