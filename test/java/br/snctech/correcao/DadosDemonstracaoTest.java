package br.snctech.correcao;

import br.snctech.correcao.corretor.*;
import br.snctech.correcao.model.*;
import br.snctech.correcao.repositorio.memoria.*;
import br.snctech.correcao.service.*;
import br.snctech.correcao.service.ResumoCorrecaoService.SituacaoAluno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Roda os serviços de verdade em cima dos repositórios de demonstração (seed). */
class DadosDemonstracaoTest {

    private static final int AVALIACAO = RepositorioQuestoesCorrecaoMemoria.ID_AVALIACAO_DEMO;

    private RepositorioAvaliacoesRespondidasMemoria respondidas;
    private RepositorioQuestoesCorrecaoMemoria questoes;
    private CorrecaoAutomaticaService automatica;
    private CorrecaoManualService manual;
    private ResumoCorrecaoService resumo;

    @BeforeEach
    void preparar() {
        respondidas = new RepositorioAvaliacoesRespondidasMemoria();
        questoes = new RepositorioQuestoesCorrecaoMemoria();
        automatica = new CorrecaoAutomaticaService(
                List.of(new CorretorMultiplaEscolha(), new CorretorAssociacao(),
                        new CorretorDiscursiva(new PreCorretorPorPalavrasChave())),
                respondidas, questoes);
        manual = new CorrecaoManualService(respondidas, questoes);
        resumo = new ResumoCorrecaoService(respondidas, new RepositorioAlunosMemoria());
        respondidas.buscarPorAvaliacao(AVALIACAO).forEach(p -> automatica.corrigir(p.getId()));
    }

    @Test
    void questoesDeDemonstracaoSomamDez() {
        assertTrue(ValoresDaAvaliacao.somaFechaDez(
                questoes.buscarPorAvaliacao(AVALIACAO).stream().map(QuestaoCorrecao::valor).toList()));
    }

    @Test
    void buscarProvaInexistenteLancaExcecaoEmVezDeDevolverNull() {
        assertThrows(IllegalArgumentException.class, () -> respondidas.buscar(999));
    }

    @Test
    void asTresSituacoesAparecemNaListaDeAlunos() {
        var linhas = resumo.resumir(AVALIACAO, false).alunos();

        assertEquals(SituacaoAluno.PENDENTE, linha(linhas, "João Silva").situacao());
        assertEquals(SituacaoAluno.CORRIGIDA, linha(linhas, "Maria Souza").situacao());
        assertEquals(SituacaoAluno.NAO_RESPONDEU, linha(linhas, "Pedro Santos").situacao());
    }

    @Test
    void joaoTemDuasDiscursivasPendentesENotaParcial() {
        AvaliacaoRespondida joao = respondidas.buscar(1);
        assertFalse(joao.notaFinalConfirmada());
        // Q1 certa (2,0) + Q2 errada (0) + Q3 metade (1,0) = 3,0
        assertEquals(0, new BigDecimal("3.00").compareTo(joao.notaTotal()));
        assertEquals(2, manual.listarPendentes(AVALIACAO).size());
    }

    @Test
    void mariaFicaComNotaFinalOitoVirgulaCinco() {
        AvaliacaoRespondida maria = respondidas.buscar(2);
        assertTrue(maria.notaFinalConfirmada());
        // 2 + 2 + 2 + 2 + 1,5 = 9,5
        assertEquals(0, new BigDecimal("9.5").compareTo(maria.notaTotal()));
    }

    @Test
    void corrigirDeNovoNaoSobrescreveNotaDoProfessor() {
        automatica.corrigir(2);
        assertEquals(0, new BigDecimal("1.5").compareTo(respondidas.buscar(2).resposta(5).get().getNota()));
    }

    @Test
    void professorFechaAProvaDoJoaoValidandoAsSugestoes() {
        manual.validarSugestao(1, 4, 99);
        manual.validarSugestao(1, 5, 99);
        assertTrue(respondidas.buscar(1).notaFinalConfirmada());
    }

    @Test
    void pedroSoRecebeZeroDepoisDoPrazo() {
        assertNull(linha(resumo.resumir(AVALIACAO, false).alunos(), "Pedro Santos").nota());
        assertEquals(0, BigDecimal.ZERO.compareTo(linha(resumo.resumir(AVALIACAO, true).alunos(), "Pedro Santos").nota()));
    }

    private static ResumoCorrecaoService.LinhaAluno linha(List<ResumoCorrecaoService.LinhaAluno> linhas, String nome) {
        return linhas.stream().filter(l -> l.nome().equals(nome)).findFirst().orElseThrow();
    }
}
