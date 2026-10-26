package br.snctech;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TurmaDAO {

    /**
     * Cadastra uma nova turma para o professor e encerra automaticamente 
     * a turma anterior (definindo ativa = false), preservando o histórico.
     */
    public static void cadastrarTurma(Connection conexao, int professorId, String nomeTurma) {
        String desativarAntigas = "UPDATE turmas SET ativa = FALSE WHERE professor_id = ? AND ativa = TRUE";
        String inserirNova = "INSERT INTO turmas (nome, professor_id, ativa) VALUES (?, ?, TRUE)";

        try {
            // Desativa o auto-commit para controlar a transação com segurança
            conexao.setAutoCommit(false);

            // 1. Desativa a turma atual ativa do professor
            try (PreparedStatement stmt1 = conexao.prepareStatement(desativarAntigas)) {
                stmt1.setInt(1, professorId);
                stmt1.executeUpdate();
            }

            // 2. Insere a nova turma como ativa
            try (PreparedStatement stmt2 = conexao.prepareStatement(inserirNova)) {
                stmt2.setString(1, nomeTurma);
                stmt2.setInt(2, professorId);
                stmt2.executeUpdate();
            }

            // Confirma as alterações no banco de dados
            conexao.commit();
            System.out.println("Sucesso: Nova turma cadastrada e turma anterior encerrada automaticamente!");

        } catch (SQLException e) {
            try {
                // Em caso de erro, desfaz as alterações (rollback)
                conexao.rollback();
            } catch (SQLException ex) {
                System.err.println("Erro no rollback: " + ex.getMessage());
            }
            System.err.println("Erro ao gerenciar turmas: " + e.getMessage());
        } finally {
            try {
                // Restaura o auto-commit padrão
                conexao.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Lista todas as turmas de um professor (ativas e histórico).
     */
    public static void listarTurmasDoProfessor(Connection conexao, int professorId) {
        String sql = "SELECT id, nome, ativa, criado_em FROM turmas WHERE professor_id = ? ORDER BY criado_em DESC";

        try (PreparedStatement pstmt = conexao.prepareStatement(sql)) {
            pstmt.setInt(1, professorId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- Histórico de Turmas do Professor ---");
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String nome = rs.getString("nome");
                    boolean ativa = rs.getBoolean("ativa");
                    String criadoEm = rs.getString("criado_em");

                    String status = ativa ? "[ATIVA]" : "[ENCERRADA]";
                    System.printf("ID: %d | Turma: %s | Status: %s | Criada em: %s%n", 
                                  id, nome, status, criadoEm);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar turmas: " + e.getMessage());
        }
    }
}