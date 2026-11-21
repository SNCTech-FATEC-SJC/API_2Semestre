package br.snctech.autenticacao.servico;

import br.snctech.autenticacao.modelo.Perfil;
import br.snctech.autenticacao.modelo.UsuarioLogado;

import java.util.Map;
import java.util.Set;

/**
 * Regras de acesso por tela. Telas fora da lista exigem apenas estar logado;
 * para restringir uma tela nova a certos perfis, acrescente-a em RESTRICOES.
 */
public final class ControleAcesso {

    public static final String TELA_LOGIN = "login.fxml";

    private static final Map<String, Set<Perfil>> RESTRICOES = Map.ofEntries(
            Map.entry("dashboard-professor.fxml", Set.of(Perfil.PROFESSOR)),
            Map.entry("banco-questoes.fxml", Set.of(Perfil.PROFESSOR)),
            Map.entry("provas-aplicadas.fxml", Set.of(Perfil.PROFESSOR)),
            Map.entry("dashboard-admin.fxml", Set.of(Perfil.ADMIN)),
            Map.entry("turmas.fxml", Set.of(Perfil.PROFESSOR, Perfil.ADMIN)),
            Map.entry("alunos.fxml", Set.of(Perfil.PROFESSOR, Perfil.ADMIN)),
            Map.entry("dashboard-aluno.fxml", Set.of(Perfil.ALUNO)),
            Map.entry("minhas-turmas.fxml", Set.of(Perfil.ALUNO))
    );

    private ControleAcesso() {
    }

    public static boolean permitido(String arquivoFxml, UsuarioLogado usuario) {
        if (TELA_LOGIN.equals(arquivoFxml)) {
            return true;
        }
        if (usuario == null) {
            return false;
        }
        Set<Perfil> perfis = RESTRICOES.get(arquivoFxml);
        return perfis == null || perfis.contains(usuario.perfil());
    }
}
