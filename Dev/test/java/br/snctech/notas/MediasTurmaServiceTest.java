package br.snctech.notas;

import br.snctech.correcao.model.NotaDoAluno;
import br.snctech.notas.model.AlunoDaTurma;
import br.snctech.notas.model.AvaliacaoDaTurma;
import br.snctech.notas.model.LinhaDoQuadro;
import br.snctech.notas.model.MediaFinal;
import br.snctech.notas.model.MediaLiberada;
import br.snctech.notas.model.QuadroDaTurma;
import br.snctech.notas.model.SituacaoFinal;
import br.snctech.notas.model.TentativaRegistrada;
import br.snctech.notas.model.TurmaResumo;
import br.snctech.notas.repositorio.RepositorioMedias;
import br.snctech.notas.service.CalculoMediaService;
import br.snctech.notas.service.MediasTurmaService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediasTurmaServiceTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 20, 12, 0);
    private static final TurmaResumo TURMA = new TurmaResumo(10, "Turma A");
    private static final AlunoDaTurma ANA = new AlunoDaTurma(1, "Ana", "ana@x.com");
    private static final AlunoDaTurma BRUNO = new AlunoDaTurma(2, "Bruno", "bruno@x.com");
    private static final AlunoDaTurma CARLA = new AlunoDaTurma(3, "Carla", "carla@x.com");

    private final RepositorioFalso repo = new RepositorioFalso();
    private final List<Integer> corrigidasAutomaticamente = new ArrayList<>();
    private final MediasTurmaService servico = new MediasTurmaService(repo, new CalculoMediaService(),
            () -> AGORA, corrigidasAutomaticamente::add);

    private static AvaliacaoDaTurma prova(int id, String peso, boolean encerrada) {
        return new AvaliacaoDaTurma(id, "P" + id, new BigDecimal(peso), encerrada ? AGORA.minusDays(1) : AGORA.plusDays(1));
    }

    private static TentativaRegistrada corrigida(AlunoDaTurma aluno, int idAvaliacao, String nota) {
        return new TentativaRegistrada(aluno.id(), idAvaliacao, "enviada", new BigDecimal(nota), true, true);
    }

    private static LinhaDoQuadro linhaDe(QuadroDaTurma quadro, AlunoDaTurma aluno) {
        return quadro.linhas().stream().filter(l -> l.aluno().id() == aluno.id()).findFirst().orElseThrow();
    }

    @Test
    void calculaAMediaPonderadaDeCadaAlunoDaTurma() {
        repo.avaliacoes.addAll(List.of(prova(100, "1", true), prova(200, "2", true)));
        repo.alunos.addAll(List.of(ANA, BRUNO, CARLA));
        repo.tentativas.addAll(List.of(
                corrigida(ANA, 100, "8.67"), corrigida(ANA, 200, "10.00"),
                corrigida(BRUNO, 100, "4.50"), corrigida(BRUNO, 200, "5.00")));

        QuadroDaTurma quadro = servico.montarQuadro(TURMA);

        assertEquals(new BigDecimal("9.56"), linhaDe(quadro, ANA).media().media());
        assertEquals(SituacaoFinal.APROVADO, linhaDe(quadro, ANA).media().situacao());
        assertEquals(new BigDecimal("4.83"), linhaDe(quadro, BRUNO).media().media());
        assertEquals(SituacaoFinal.EXAME_FINAL, linhaDe(quadro, BRUNO).media().situacao());
        assertEquals(new BigDecimal("0.00"), linhaDe(quadro, CARLA).media().media());
        assertEquals(SituacaoFinal.REPROVADO, linhaDe(quadro, CARLA).media().situacao());
        assertTrue(quadro.podeLiberar());
    }

    @Test
    void mostraANotaDeCadaProvaParaOProfessor() {
        repo.avaliacoes.add(prova(100, "1", true));
        repo.alunos.addAll(List.of(ANA, CARLA));
        repo.tentativas.add(corrigida(ANA, 100, "7.50"));

        QuadroDaTurma quadro = servico.montarQuadro(TURMA);

        assertEquals("7,50", linhaDe(quadro, ANA).textoDaNota(100));
        assertEquals("0,00  (não realizada)", linhaDe(quadro, CARLA).textoDaNota(100));
    }

    @Test
    void gravaNoBancoSoAsMediasProntas() {
        repo.avaliacoes.add(prova(100, "1", true));
        repo.alunos.addAll(List.of(ANA, BRUNO));
        repo.tentativas.add(corrigida(ANA, 100, "9.00"));
        repo.tentativas.add(new TentativaRegistrada(BRUNO.id(), 100, "enviada", null, false, false));

        QuadroDaTurma quadro = servico.montarQuadro(TURMA);

        assertEquals(List.of(ANA.id()), repo.sincronizadas.stream().map(MediaFinal::idAluno).toList());
        assertFalse(quadro.podeLiberar());
        assertEquals(1, quadro.pendencias().size());
        assertTrue(quadro.pendencias().get(0).startsWith("Bruno"));
    }

    @Test
    void provaAindaNoPrazoNaoMostraNotaEDeixaAMediaPendente() {
        repo.avaliacoes.add(prova(100, "1", false));
        repo.alunos.add(ANA);

        QuadroDaTurma quadro = servico.montarQuadro(TURMA);

        assertEquals("Aguardando prazo", linhaDe(quadro, ANA).textoDaNota(100));
        assertFalse(linhaDe(quadro, ANA).media().estaPronta());
        assertTrue(repo.sincronizadas.isEmpty());
    }

    @Test
    void corrigeAutomaticamenteAsProvasDaTurmaAntesDeCalcular() {
        repo.avaliacoes.addAll(List.of(prova(100, "1", true), prova(200, "2", true)));

        servico.montarQuadro(TURMA);

        assertEquals(List.of(100, 200), corrigidasAutomaticamente);
    }

    @Test
    void naoLiberaComMediasPendentes() {
        repo.avaliacoes.add(prova(100, "1", true));
        repo.alunos.add(ANA);
        repo.tentativas.add(new TentativaRegistrada(ANA.id(), 100, "enviada", null, false, false));

        assertThrows(IllegalStateException.class, () -> servico.liberar(TURMA));
        assertFalse(repo.liberou);
    }

    @Test
    void naoLiberaTurmaSemAlunos() {
        repo.avaliacoes.add(prova(100, "1", true));

        assertThrows(IllegalStateException.class, () -> servico.liberar(TURMA));
    }

    @Test
    void liberaQuandoTodasAsMediasEstaoProntas() {
        repo.avaliacoes.add(prova(100, "1", true));
        repo.alunos.add(ANA);
        repo.tentativas.add(corrigida(ANA, 100, "6.00"));

        QuadroDaTurma quadro = servico.liberar(TURMA);

        assertTrue(repo.liberou);
        assertTrue(quadro.todasLiberadas());
        assertEquals("Liberada", linhaDe(quadro, ANA).textoDaLiberacao());
    }

    @Test
    void recalculaAMediaQuandoANotaMudaNaCorrecaoManual() {
        repo.avaliacoes.add(prova(100, "1", true));
        repo.alunos.add(ANA);
        repo.tentativas.add(corrigida(ANA, 100, "5.00"));
        servico.liberar(TURMA);
        assertEquals(new BigDecimal("5.00"), repo.mediaGravada(ANA.id()));

        repo.tentativas.set(0, corrigida(ANA, 100, "7.00"));
        servico.recalcularPelaAvaliacao(100);

        assertEquals(new BigDecimal("7.00"), repo.mediaGravada(ANA.id()));
        assertTrue(repo.liberacaoPorAluno(TURMA.id()).get(ANA.id()));
    }

    @Test
    void provaCorrigidaMasNaoFinalizadaDeixaAMediaPendente() {
        repo.avaliacoes.add(prova(100, "1", true));
        repo.alunos.add(ANA);
        repo.tentativas.add(new TentativaRegistrada(ANA.id(), 100, "enviada", new BigDecimal("8.00"), true, false));

        QuadroDaTurma quadro = servico.montarQuadro(TURMA);

        assertEquals("Aguardando Correção do Professor", linhaDe(quadro, ANA).textoDaNota(100));
        assertFalse(linhaDe(quadro, ANA).media().estaPronta());
        assertFalse(quadro.podeLiberar());
    }

    @Test
    void recalcularDeAvaliacaoSemTurmaNaoFazNada() {
        servico.recalcularPelaAvaliacao(999);
        assertTrue(repo.sincronizadas.isEmpty());
    }

    private static final class RepositorioFalso implements RepositorioMedias {
        final List<AvaliacaoDaTurma> avaliacoes = new ArrayList<>();
        final List<AlunoDaTurma> alunos = new ArrayList<>();
        final List<TentativaRegistrada> tentativas = new ArrayList<>();
        List<MediaFinal> sincronizadas = new ArrayList<>();
        final Map<Integer, Boolean> liberacao = new HashMap<>();
        boolean liberou;

        @Override public List<TurmaResumo> turmasDoProfessor(int idProfessor) { return List.of(TURMA); }
        @Override public List<AvaliacaoDaTurma> avaliacoesDaTurma(int idTurma) { return avaliacoes; }
        @Override public List<AlunoDaTurma> alunosDaTurma(int idTurma) { return alunos; }
        @Override public List<TentativaRegistrada> tentativasDaTurma(int idTurma) { return tentativas; }

        @Override
        public void sincronizar(int idTurma, List<MediaFinal> mediasProntas) {
            sincronizadas = new ArrayList<>(mediasProntas);
            liberacao.keySet().retainAll(mediasProntas.stream().map(MediaFinal::idAluno).toList());
            mediasProntas.forEach(m -> liberacao.putIfAbsent(m.idAluno(), false));
        }

        @Override public Map<Integer, Boolean> liberacaoPorAluno(int idTurma) { return Map.copyOf(liberacao); }

        @Override
        public void liberar(int idTurma) {
            liberou = true;
            liberacao.replaceAll((aluno, liberada) -> true);
        }

        @Override
        public Integer turmaDaAvaliacao(int idAvaliacao) {
            return avaliacoes.stream().anyMatch(a -> a.id() == idAvaliacao) ? TURMA.id() : null;
        }

        @Override public List<MediaLiberada> mediasLiberadasDoAluno(String email) { return List.of(); }

        BigDecimal mediaGravada(int idAluno) {
            return sincronizadas.stream().filter(m -> m.idAluno() == idAluno).findFirst().orElseThrow().media();
        }
    }
}
