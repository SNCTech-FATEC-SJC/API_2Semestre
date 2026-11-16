package br.snctech.banco;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Consultas à tabela usuarios (enquanto não existe a tela de login, PB-19). */
public final class UsuarioDAO {
    private UsuarioDAO() { }

    public record Usuario(int id, String nome, String email) { }

    /** Provisório: sem login, o professor "logado" é o primeiro cadastrado. null se não há nenhum. */
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
}
