package br.snctech.autenticacao.servico;

import br.snctech.autenticacao.modelo.Perfil;
import br.snctech.autenticacao.modelo.UsuarioLogado;
import br.snctech.banco.Conexao;
import br.snctech.banco.UsuarioDAO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Valida e-mail e senha consultando a tabela usuarios.
 * A senha é comparada como texto simples (senha_hash guarda a senha sem hash neste projeto).
 */
public final class AutenticacaoServico {

    private static final String MENSAGEM_INVALIDA = "E-mail ou senha incorretos.";

    private AutenticacaoServico() {
    }

    /**
     * @throws IllegalArgumentException credenciais vazias, incorretas ou perfil desconhecido
     * @throws br.snctech.banco.ErroBanco falha de acesso ao banco
     */
    public static UsuarioLogado autenticar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException("Informe o e-mail e a senha.");
        }
        String emailLimpo = email.trim();
        UsuarioDAO.Credencial credencial = Conexao.executar(c -> UsuarioDAO.buscarCredencialPorEmail(c, emailLimpo));

        // Mesma mensagem para e-mail inexistente e senha errada (não revela quais e-mails existem).
        if (credencial == null || !senhaConfere(senha, credencial.senha())) {
            throw new IllegalArgumentException(MENSAGEM_INVALIDA);
        }
        Perfil perfil = Perfil.deBanco(credencial.perfil());
        if (perfil == null) {
            throw new IllegalArgumentException("O perfil deste usuário não é reconhecido. Procure o administrador.");
        }
        return new UsuarioLogado(credencial.id(), credencial.nome(), credencial.email(), perfil);
    }

    private static boolean senhaConfere(String digitada, String armazenada) {
        if (armazenada == null) {
            return false;
        }
        return MessageDigest.isEqual(
                digitada.getBytes(StandardCharsets.UTF_8),
                armazenada.getBytes(StandardCharsets.UTF_8));
    }
}
