package br.snctech;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestarConexao {

    // Ajustado com o nome do seu banco e a sua senha
    private static final String URL = "jdbc:postgresql://localhost:5432/SNCTech_DB";
    private static final String USUARIO = "postgres";
    private static final String SENHA = "123654";

    public static void main(String[] args) {
        System.out.println("Testando a conexão com o banco de dados...");

        try (Connection conexao = DriverManager.getConnection(URL, USUARIO, SENHA)) {
            if (conexao != null && !conexao.isClosed()) {
                System.out.println("SUCESSO! Conectado ao PostgreSQL com sucesso!");
            }
        } catch (SQLException e) {
            System.err.println("FALHA na conexão! Verifique a URL, usuário ou senha.");
            System.err.println("Erro: " + e.getMessage());
        }
    }
}