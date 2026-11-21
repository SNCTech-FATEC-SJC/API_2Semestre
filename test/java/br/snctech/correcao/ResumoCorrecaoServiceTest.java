package br.snctech.correcao;

import br.snctech.correcao.model.*;
import br.snctech.correcao.service.ResumoCorrecaoService;
import br.snctech.correcao.service.ResumoCorrecaoService.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResumoCorrecaoServiceTest {

    private ResumoCorrecaoService service;

    private AvaliacaoRespondida prova(Integer id, Integer idAluno, String notaMc, StatusCorrecao statusDisc, String notaDisc) {
        AvaliacaoRespondida ar = new AvaliacaoRespondida(id, 100, idAluno);
        RespostaQuestao mc = new RespostaQuestao(1);
        mc.setNota(new BigDecimal(notaMc));
        mc.setStatus(StatusCorrecao.CORRIGIDA_AUTOMATICAMENTE);
        RespostaQuestao disc = new RespostaQuestao(2);
        disc.setStatus(statusDisc);
        disc.setNota(notaDisc == null ? null : new BigDecimal(notaDisc));
        ar.adicionar(mc);
        ar.adicionar(disc);
        return ar;
    }

    @BeforeEach
    void preparar() {
        Fakes.RespondidaEmMemoria respondidas = new Fakes.RespondidaEmMemoria();
        respondidas.salvar(prova(1, 1, "5.0", StatusCorrecao.PENDENTE_MANUAL, null));          // Ana: pendente
        respondidas.salvar(prova(2, 2, "5.0", StatusCorrecao.CORRIGIDA_MANUALMENTE, "3.0"));   // Bruno: 8,0
        Fakes.AlunoEmMemoria alunos = new Fakes.AlunoEmMemoria(List.of(
                new AlunoResumo(3, "Diego Rocha"),   // não respondeu
                new AlunoResumo(1, "Ana Souza"),
                new AlunoResumo(2, "Bruno Lima")));
        service = new ResumoCorrecaoService(respondidas, alunos);
    }

    @Test
    void montaLinhasOrdenadasPorNome() {
        ResumoAvaliacao resumo = service.resumir(100, true);

        assertEquals(List.of("Ana Souza", "Bruno Lima", "Diego Rocha"),
                resumo.alunos().stream().map(LinhaAluno::nome).toList());

        LinhaAluno ana = resumo.alunos().get(0);
        assertEquals(SituacaoAluno.PENDENTE, ana.situacao());
        assertEquals(1, ana.discursivasPendentes());
        assertNull(ana.nota());

        LinhaAluno bruno = resumo.alunos().get(1);
        assertEquals(SituacaoAluno.CORRIGIDA, bruno.situacao());
        assertEquals(0, new BigDecimal("8.0").compareTo(bruno.nota()));
    }

    @Test
    void semRespostaRecebeZeroSoDepoisDoPrazo() {
        LinhaAluno diegoAposPrazo = service.resumir(100, true).alunos().get(2);
        assertEquals(SituacaoAluno.NAO_RESPONDEU, diegoAposPrazo.situacao());
        assertEquals(0, BigDecimal.ZERO.compareTo(diegoAposPrazo.nota()));

        LinhaAluno diegoAntesPrazo = service.resumir(100, false).alunos().get(2);
        assertNull(diegoAntesPrazo.nota());
    }

    @Test
    void percentualCorrigidoConsideraPrazo() {
        assertEquals(67, service.resumir(100, true).percentualCorrigido());  // Bruno + Diego de 3
        assertEquals(33, service.resumir(100, false).percentualCorrigido()); // só Bruno de 3
    }
}
