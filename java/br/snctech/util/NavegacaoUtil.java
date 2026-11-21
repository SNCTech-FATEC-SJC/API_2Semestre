package br.snctech.util;

import br.snctech.autenticacao.modelo.UsuarioLogado;
import br.snctech.autenticacao.servico.ControleAcesso;
import br.snctech.banco.Conexao;
import br.snctech.banco.SessaoUsuario;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Troca o conteudo da janela atual por outra tela, respeitando o perfil do usuario logado.
 * Use a partir de qualquer controller: NavegacaoUtil.abrir(botaoEntrar, "turmas.fxml", "SNCTECH - Turmas");
 *
 * Sem login, qualquer tela (exceto o login) redireciona para o login.
 * Com login, telas restritas a outros perfis sao bloqueadas (ver ControleAcesso).
 */
public final class NavegacaoUtil {

    private static final String TITULO_LOGIN = "SNCTECH - Login";

    private NavegacaoUtil() {
    }

    public static void abrir(Node origem, String arquivoFxml, String titulo) {
        UsuarioLogado usuario = SessaoUsuario.getUsuario();
        String destino = arquivoFxml;
        String tituloFinal = titulo;

        if (!ControleAcesso.permitido(arquivoFxml, usuario)) {
            if (usuario == null) {
                destino = ControleAcesso.TELA_LOGIN;
                tituloFinal = TITULO_LOGIN;
            } else {
                DialogoUtil.erro(origem, "Você não tem permissão para acessar esta tela.");
                return;
            }
        }
        carregar(origem, destino, tituloFinal);
    }

    /** Abre a tela inicial (dashboard) do perfil logado; sem login, abre o login. */
    public static void abrirInicio(Node origem) {
        UsuarioLogado usuario = SessaoUsuario.getUsuario();
        if (usuario == null) {
            carregar(origem, ControleAcesso.TELA_LOGIN, TITULO_LOGIN);
        } else {
            carregar(origem, usuario.perfil().arquivoDashboard(), usuario.perfil().tituloJanela());
        }
    }

    /** Pede confirmacao, encerra a sessao e volta ao login. */
    public static void sairDaConta(Node origem) {
        if (DialogoUtil.confirmar(origem, "Sair da conta", "Deseja sair da sua conta?")) {
            SessaoUsuario.encerrar();
            carregar(origem, ControleAcesso.TELA_LOGIN, TITULO_LOGIN);
        }
    }

    /** Pede confirmacao, encerra a sessao, fecha a conexao com o banco e fecha a aplicacao. */
    public static void fecharAplicacao(Node origem) {
        if (DialogoUtil.confirmar(origem, "Fechar aplicação", "Deseja fechar a aplicação?")) {
            SessaoUsuario.encerrar();
            Conexao.fechar();
            Platform.exit();
        }
    }

    private static void carregar(Node origem, String arquivoFxml, String titulo) {
        try {
            Parent raiz = FXMLLoader.load(NavegacaoUtil.class.getResource("/br/snctech/" + arquivoFxml));

            Stage palco = (Stage) origem.getScene().getWindow();
            Scene cena = new Scene(raiz, palco.getWidth(), palco.getHeight());
            cena.getStylesheets().add(
                    NavegacaoUtil.class.getResource("/br/snctech/styles.css").toExternalForm());

            palco.setTitle(titulo);
            palco.setScene(cena);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível abrir a tela: " + arquivoFxml, e);
        }
    }
}
