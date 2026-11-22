package br.snctech.notas;

import br.snctech.correcao.model.NotaDoAluno;
import br.snctech.correcao.model.NotaDoAluno.Situacao;
import br.snctech.notas.model.MediaFinal;
import br.snctech.notas.model.NotaPonderada;
import br.snctech.notas.model.SituacaoFinal;
import br.snctech.notas.service.CalculoMediaService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculoMediaServiceTest {
    private final CalculoMediaService servico = new CalculoMediaService();

    private static NotaPonderada corrigida(int id, String nota, String peso) {
        return new NotaPonderada(id, "P" + id, new BigDecimal(peso), true, Situacao.NOTA, new BigDecimal(nota));
    }

    private static NotaPonderada naoFez(int id, String peso) {
        return new NotaPonderada(id, "P" + id, new BigDecimal(peso), true, Situacao.NAO_REALIZADA, BigDecimal.ZERO);
    }

    private static NotaPonderada emCorrecao(int id) {
        return new NotaPonderada(id, "P" + id, BigDecimal.ONE, true, Situacao.EM_CORRECAO, null);
    }

    private MediaFinal calcular(NotaPonderada... notas) {
        return servico.calcular(2, "Ana Maria", List.of(notas));
    }

    @Test
    void mediaPonderadaUsaOsPesosDoProfessor() {
        MediaFinal m = calcular(corrigida(1, "8.00", "2"), corrigida(2, "5.00", "3"));
        assertEquals(new BigDecimal("6.20"), m.media());
        assertEquals(SituacaoFinal.APROVADO, m.situacao());
        assertTrue(m.pendencias().isEmpty());
    }

    @Test
    void pesosIguaisDaoAMediaSimples() {
        MediaFinal m = calcular(corrigida(1, "7.00", "1.00"), corrigida(2, "4.00", "1.00"));
        assertEquals(new BigDecimal("5.50"), m.media());
    }

    @Test
    void pesoComCasaDecimalTambemFunciona() {
        MediaFinal m = calcular(corrigida(1, "10.00", "2.5"), corrigida(2, "4.00", "1"));
        assertEquals(new BigDecimal("8.29"), m.media());
    }

    @Test
    void quemNaoFezAProvaEntraComZero() {
        MediaFinal m = calcular(corrigida(1, "10.00", "1"), naoFez(2, "1"));
        assertEquals(new BigDecimal("5.00"), m.media());
        assertEquals(SituacaoFinal.EXAME_FINAL, m.situacao());
    }

    @Test
    void mediaEhArredondadaParaDuasCasasComMeioParaCima() {
        MediaFinal m = calcular(corrigida(1, "5.99", "1"), corrigida(2, "6.00", "1"));
        assertEquals(new BigDecimal("6.00"), m.media());
        assertEquals(SituacaoFinal.APROVADO, m.situacao());
    }

    @Test
    void umaUnicaAvaliacaoViraAPropriaMedia() {
        MediaFinal m = calcular(corrigida(1, "8.67", "1"));
        assertEquals(new BigDecimal("8.67"), m.media());
    }

    @Test
    void seisOuMaisEhAprovado() {
        assertEquals(SituacaoFinal.APROVADO, servico.situacaoPara(new BigDecimal("6.00")));
        assertEquals(SituacaoFinal.APROVADO, servico.situacaoPara(new BigDecimal("10.00")));
    }

    @Test
    void entreQuatroESeisEhExameFinal() {
        assertEquals(SituacaoFinal.EXAME_FINAL, servico.situacaoPara(new BigDecimal("4.00")));
        assertEquals(SituacaoFinal.EXAME_FINAL, servico.situacaoPara(new BigDecimal("5.99")));
    }

    @Test
    void abaixoDeQuatroEhReprovado() {
        assertEquals(SituacaoFinal.REPROVADO, servico.situacaoPara(new BigDecimal("3.99")));
        assertEquals(SituacaoFinal.REPROVADO, servico.situacaoPara(BigDecimal.ZERO));
    }

    @Test
    void discursivaSemCorrecaoDeixaAMediaPendente() {
        MediaFinal m = calcular(corrigida(1, "9.00", "1"), emCorrecao(2));
        assertEquals(SituacaoFinal.PENDENTE, m.situacao());
        assertNull(m.media());
        assertFalse(m.estaPronta());
        assertEquals(List.of("P2: aguardando a correção do professor."), m.pendencias());
    }

    @Test
    void provaAindaNoPrazoDeixaAMediaPendenteMesmoComNota() {
        NotaPonderada noPrazo = new NotaPonderada(2, "P2", BigDecimal.ONE, false, Situacao.NOTA, new BigDecimal("7.00"));
        MediaFinal m = calcular(corrigida(1, "9.00", "1"), noPrazo);
        assertEquals(SituacaoFinal.PENDENTE, m.situacao());
        assertEquals(List.of("P2: o prazo ainda não terminou."), m.pendencias());
    }

    @Test
    void provaEmAndamentoDeixaAMediaPendente() {
        NotaPonderada andamento = new NotaPonderada(1, "P1", BigDecimal.ONE, true, Situacao.EM_ANDAMENTO, null);
        MediaFinal m = calcular(andamento);
        assertEquals(List.of("P1: a prova ainda está em andamento."), m.pendencias());
    }

    @Test
    void todasAsPendenciasSaoListadas() {
        NotaPonderada noPrazo = new NotaPonderada(3, "P3", BigDecimal.ONE, false, Situacao.NAO_REALIZADA, BigDecimal.ZERO);
        MediaFinal m = calcular(emCorrecao(1), corrigida(2, "8.00", "1"), noPrazo);
        assertEquals(2, m.pendencias().size());
    }

    @Test
    void turmaSemAvaliacoesNaoTemMedia() {
        MediaFinal m = servico.calcular(2, "Ana Maria", List.of());
        assertEquals(SituacaoFinal.PENDENTE, m.situacao());
        assertEquals(1, m.pendencias().size());
    }

    @Test
    void professorSoLiberaQuandoTodosTemMedia() {
        MediaFinal ana = calcular(corrigida(1, "8.00", "1"));
        MediaFinal bruno = servico.calcular(5, "Bruno Lima", List.of(corrigida(1, "4.50", "1")));
        MediaFinal carla = servico.calcular(6, "Carla Souza", List.of(emCorrecao(1)));

        assertTrue(servico.podeLiberar(List.of(ana, bruno)));
        assertFalse(servico.podeLiberar(List.of(ana, bruno, carla)));
        assertFalse(servico.podeLiberar(List.of()));
    }

    @Test
    void pesoZeroNegativoOuVazioEhRejeitado() {
        assertThrows(IllegalArgumentException.class, () -> corrigida(1, "8.00", "0"));
        assertThrows(IllegalArgumentException.class, () -> corrigida(1, "8.00", "-1"));
        assertThrows(IllegalArgumentException.class,
                () -> new NotaPonderada(1, "P1", null, true, Situacao.NOTA, BigDecimal.TEN));
    }

    @Test
    void notaForaDeZeroADezEhRejeitada() {
        assertThrows(IllegalArgumentException.class, () -> corrigida(1, "10.01", "1"));
        assertThrows(IllegalArgumentException.class, () -> corrigida(1, "-0.01", "1"));
        assertThrows(IllegalArgumentException.class,
                () -> new NotaPonderada(1, "P1", BigDecimal.ONE, true, Situacao.NOTA, null));
    }

    @Test
    void montaANotaPonderadaAPartirDaNotaDoAluno() {
        LocalDateTime prazo = LocalDateTime.of(2026, 11, 13, 23, 59);
        NotaDoAluno nuncaAbriu = NotaDoAluno.de(1, "P1", prazo, null, null, false, false);
        NotaDoAluno corrigidaPeloProfessor = NotaDoAluno.de(2, "P2", prazo, "enviada", new BigDecimal("9.00"), true, true);

        MediaFinal m = calcular(NotaPonderada.de(nuncaAbriu, new BigDecimal("1"), true),
                NotaPonderada.de(corrigidaPeloProfessor, new BigDecimal("3"), true));

        assertEquals(new BigDecimal("6.75"), m.media());
        assertEquals(SituacaoFinal.APROVADO, m.situacao());
    }

    @Test
    void textoDaMediaParaATela() {
        assertEquals("6,20", calcular(corrigida(1, "8.00", "2"), corrigida(2, "5.00", "3")).textoDaMedia());
        assertEquals("–", calcular(emCorrecao(1)).textoDaMedia());
        assertEquals("Exame final", SituacaoFinal.EXAME_FINAL.rotulo());
    }
}
