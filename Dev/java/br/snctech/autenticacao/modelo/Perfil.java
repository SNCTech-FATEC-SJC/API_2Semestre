package br.snctech.autenticacao.modelo;

/**
 * Perfis do sistema (enum perfil_usuario do banco) e a área para a qual cada um é direcionado.
 */
public enum Perfil {
    PROFESSOR("professor", "Professor", "dashboard-professor.fxml", "SNCTECH - Professor"),
    ALUNO("aluno", "Aluno", "dashboard-aluno.fxml", "SNCTECH - Aluno"),
    ADMIN("admin", "Administrador", "dashboard-admin.fxml", "SNCTECH - Administrador");

    private final String valorBanco;
    private final String rotulo;
    private final String arquivoDashboard;
    private final String tituloJanela;

    Perfil(String valorBanco, String rotulo, String arquivoDashboard, String tituloJanela) {
        this.valorBanco = valorBanco;
        this.rotulo = rotulo;
        this.arquivoDashboard = arquivoDashboard;
        this.tituloJanela = tituloJanela;
    }

    public String valorBanco() {
        return valorBanco;
    }

    public String rotulo() {
        return rotulo;
    }

    /** Tela inicial (dashboard) do perfil. */
    public String arquivoDashboard() {
        return arquivoDashboard;
    }

    public String tituloJanela() {
        return tituloJanela;
    }

    /** Converte o texto do banco; null se vazio ou desconhecido. */
    public static Perfil deBanco(String valor) {
        if (valor == null) {
            return null;
        }
        for (Perfil perfil : values()) {
            if (perfil.valorBanco.equalsIgnoreCase(valor.trim())) {
                return perfil;
            }
        }
        return null;
    }
}
