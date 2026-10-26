package br.snctech.correcao;

import br.snctech.correcao.model.NotaDoAluno;
import br.snctech.correcao.model.NotaDoAluno.Situacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NotaDoAlunoTest {

    private static final LocalDateTime PRAZO = LocalDateTime.of(2026, 10, 24, 18, 0);

    @Test
    void provaCorrigidaMostraANota() {
        NotaDoAluno n = NotaDoAluno.de(1, "P1", PRAZO, "enviada", new BigDecimal("7.09"), true);
        assertEquals(Situacao.NOTA, n.situacao());
        assertEquals("7,09", n.textoDaNota());
    }

    @Test
    void provaComDiscursivaPendenteNaoMostraNota() {
        NotaDoAluno n = NotaDoAluno.de(1, "P1", PRAZO, "enviada", new BigDecimal("5.00"), false);
        assertEquals(Situacao.EM_CORRECAO, n.situacao());
        assertNull(n.nota());
        assertEquals("Em correção", n.textoDaNota());
    }

    @Test
    void provaEncerradaPeloPrazoSegueAMesmaRegra() {
        assertEquals(Situacao.EM_CORRECAO,
                NotaDoAluno.de(1, "P1", PRAZO, "nao_realizada_prazo", BigDecimal.ZERO, false).situacao());
        assertEquals(Situacao.NOTA,
                NotaDoAluno.de(1, "P1", PRAZO, "nao_realizada_prazo", new BigDecimal("3.5"), true).situacao());
    }

    @Test
    void quemNuncaAbriuAProvaFicaComZero() {
        NotaDoAluno n = NotaDoAluno.de(1, "P1", PRAZO, null, null, false);
        assertEquals(Situacao.NAO_REALIZADA, n.situacao());
        assertEquals("0,00  (não realizada)", n.textoDaNota());
    }

    @Test
    void provaEmAndamentoNaoTemNota() {
        NotaDoAluno n = NotaDoAluno.de(1, "P1", PRAZO, "em_andamento", null, false);
        assertEquals(Situacao.EM_ANDAMENTO, n.situacao());
        assertEquals("Em andamento", n.textoDaNota());
    }
}
