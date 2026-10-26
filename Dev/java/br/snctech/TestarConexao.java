package br.snctech;

import br.snctech.banco.Conexao;
import br.snctech.banco.ErroBanco;

/** Testa a mesma configuração de banco que será usada pela aplicação. */
public class TestarConexao {
    public static void main(String[] args) {
        System.out.println("Testando a conexão com o PostgreSQL configurado em banco.properties...");
        try {
            Conexao.executar(conexao -> {
                try (var consulta = conexao.createStatement();
                     var resultado = consulta.executeQuery("SELECT current_database(), current_user")) {
                    if (resultado.next()) {
                        System.out.println("Conexão estabelecida com sucesso!");
                        System.out.println("Banco: " + resultado.getString(1));
                        System.out.println("Usuário: " + resultado.getString(2));
                    }
                }
                return null;
            });
        } catch (ErroBanco e) {
            System.err.println("Não foi possível conectar ao PostgreSQL.");
            System.err.println(e.getMessage());
            System.err.println("Confira o arquivo banco.properties e se o serviço PostgreSQL está iniciado.");
            System.exit(1);
        }
    }
}
