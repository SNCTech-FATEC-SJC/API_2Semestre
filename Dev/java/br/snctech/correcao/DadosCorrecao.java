package br.snctech.correcao;

import br.snctech.correcao.corretor.CorretorAssociacao;
import br.snctech.correcao.corretor.CorretorDiscursiva;
import br.snctech.correcao.corretor.CorretorMultiplaEscolha;
import br.snctech.correcao.corretor.PreCorretorPorPalavrasChave;
import br.snctech.correcao.repositorio.FonteDeEnunciados;
import br.snctech.correcao.repositorio.RepositorioAlunos;
import br.snctech.correcao.repositorio.RepositorioAvaliacoesRespondidas;
import br.snctech.correcao.repositorio.RepositorioQuestoesCorrecao;
import br.snctech.correcao.repositorio.memoria.RepositorioAlunosMemoria;
import br.snctech.correcao.repositorio.memoria.RepositorioAvaliacoesRespondidasMemoria;
import br.snctech.correcao.repositorio.memoria.RepositorioQuestoesCorrecaoMemoria;
import br.snctech.correcao.service.CorrecaoAutomaticaService;
import br.snctech.correcao.service.CorrecaoManualService;
import br.snctech.correcao.service.ResultadoProvaService;
import br.snctech.correcao.service.ResumoCorrecaoService;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntPredicate;

import javafx.scene.Node;

/**
 * Ponto único que monta repositórios e serviços da Correção e os entrega aos controllers,
 * no mesmo estilo do {@code DadosAplicacao} do grupo.
 *
 * <p>Quem usa de fora (PB-07, telas de professor/aluno, PB-12):
 * {@code DadosCorrecao.correcaoAutomatica()}, {@code correcaoManual()}, {@code resumo()}, {@code resultadoProva()}.
 *
 * <p>Fonte dos dados: {@link #usarMemoria()} (padrão, com dados de demonstração). Quando o banco existir,
 * quem o implementar liga os repositórios dele com {@link #configurar}. Só este arquivo conhece essa escolha.
 */
public final class DadosCorrecao {

    /** Professor logado. Fixo até o login (PB-19) existir; a integração troca por {@link #definirProfessor(int)}. */
    private static int idProfessor = 99;

    private static CorrecaoAutomaticaService automatica;
    private static CorrecaoManualService manual;
    private static ResumoCorrecaoService resumo;
    private static ResultadoProvaService resultado;
    private static RepositorioAvaliacoesRespondidas respondidas;
    private static RepositorioQuestoesCorrecao questoes;
    private static FonteDeEnunciados enunciados;

    /** Diz se o prazo da avaliação já acabou (PB-18). Padrão: não acabou. Integração: usar Avaliacao.getPrazo(). */
    private static IntPredicate prazoEncerrado = idAvaliacao -> false;

    private DadosCorrecao() {
    }

    // ------------------------------------------------------------ escolha da fonte de dados

    /** Dados em memória + seed de demonstração (somem ao fechar o programa). */
    public static synchronized void usarMemoria() {
        RepositorioQuestoesCorrecaoMemoria q = new RepositorioQuestoesCorrecaoMemoria();
        RepositorioAvaliacoesRespondidasMemoria r = new RepositorioAvaliacoesRespondidasMemoria();
        configurar(r, q, new RepositorioAlunosMemoria(), q);
        // Simula o que o PB-07 fará ao receber cada prova: corrigir automaticamente.
        r.buscarPorAvaliacao(RepositorioQuestoesCorrecaoMemoria.ID_AVALIACAO_DEMO)
                .forEach(prova -> automatica.corrigir(prova.getId()));
    }

    /** Para ligar repositórios próprios (banco de dados, testes, integração). */
    public static synchronized void configurar(RepositorioAvaliacoesRespondidas repoRespondidas,
                                               RepositorioQuestoesCorrecao repoQuestoes,
                                               RepositorioAlunos repoAlunos,
                                               FonteDeEnunciados fonteEnunciados) {
        respondidas = repoRespondidas;
        questoes = repoQuestoes;
        enunciados = fonteEnunciados;
        automatica = new CorrecaoAutomaticaService(
                List.of(new CorretorMultiplaEscolha(),
                        new CorretorAssociacao(),
                        new CorretorDiscursiva(new PreCorretorPorPalavrasChave())),
                repoRespondidas, repoQuestoes);
        manual = new CorrecaoManualService(repoRespondidas, repoQuestoes);
        resumo = new ResumoCorrecaoService(repoRespondidas, repoAlunos);
        resultado = new ResultadoProvaService(repoRespondidas, repoQuestoes);
    }

    private static void garantir() {
        if (automatica == null) {
            usarMemoria();
        }
    }

    // ------------------------------------------------------------ acesso

    public static CorrecaoAutomaticaService correcaoAutomatica() { garantir(); return automatica; }
    public static CorrecaoManualService correcaoManual() { garantir(); return manual; }
    public static ResumoCorrecaoService resumo() { garantir(); return resumo; }
    public static ResultadoProvaService resultadoProva() { garantir(); return resultado; }
    public static RepositorioAvaliacoesRespondidas respondidas() { garantir(); return respondidas; }
    public static RepositorioQuestoesCorrecao questoes() { garantir(); return questoes; }
    public static FonteDeEnunciados enunciados() { garantir(); return enunciados; }

    public static int idProfessor() { return idProfessor; }
    public static void definirProfessor(int id) { idProfessor = id; }

    public static boolean prazoEncerrado(int idAvaliacao) { return prazoEncerrado.test(idAvaliacao); }
    public static void definirVerificadorDePrazo(IntPredicate verificador) { prazoEncerrado = verificador; }

    // ------------------------------------------------------------ contexto da tela atual

    /** Qual avaliação está sendo corrigida e para onde o botão "Voltar" da Tela 6 leva. */
    public record Contexto(int idAvaliacao, String nomeAvaliacao, String nomeTurma, Consumer<Node> aoVoltar) {}

    private static Contexto contexto = new Contexto(
            RepositorioQuestoesCorrecaoMemoria.ID_AVALIACAO_DEMO, "Prova 1", "Turma 1", null);

    /** Chamar antes de abrir {@code correcao-alunos.fxml}. {@code aoVoltar} pode ser null. */
    public static void prepararCorrecao(int idAvaliacao, String nomeAvaliacao, String nomeTurma, Consumer<Node> aoVoltar) {
        contexto = new Contexto(idAvaliacao, nomeAvaliacao, nomeTurma, aoVoltar);
    }

    public static Contexto contexto() { return contexto; }

    /** Aluno selecionado ao abrir {@code correcao-manual.fxml} (null = o primeiro da lista). */
    private static Integer alunoSelecionado;

    public static void selecionarAluno(Integer idAluno) { alunoSelecionado = idAluno; }
    public static Integer alunoSelecionado() { return alunoSelecionado; }
}
