package br.snctech.notas.service;

import br.snctech.correcao.model.NotaDoAluno;
import br.snctech.notas.model.AlunoDaTurma;
import br.snctech.notas.model.AvaliacaoDaTurma;
import br.snctech.notas.model.LinhaDoQuadro;
import br.snctech.notas.model.MediaFinal;
import br.snctech.notas.model.MediaLiberada;
import br.snctech.notas.model.NotaPonderada;
import br.snctech.notas.model.QuadroDaTurma;
import br.snctech.notas.model.TentativaRegistrada;
import br.snctech.notas.model.TurmaResumo;
import br.snctech.notas.repositorio.RepositorioMedias;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class MediasTurmaService {

    private final RepositorioMedias repositorio;
    private final CalculoMediaService calculo;
    private final Supplier<LocalDateTime> relogio;
    private final IntConsumer correcaoAutomatica;

    public MediasTurmaService(RepositorioMedias repositorio, CalculoMediaService calculo,
                              Supplier<LocalDateTime> relogio, IntConsumer correcaoAutomatica) {
        this.repositorio = repositorio;
        this.calculo = calculo;
        this.relogio = relogio;
        this.correcaoAutomatica = correcaoAutomatica;
    }

    public List<TurmaResumo> turmasDoProfessor(int idProfessor) {
        return repositorio.turmasDoProfessor(idProfessor);
    }

    public QuadroDaTurma montarQuadro(TurmaResumo turma) {
        LocalDateTime agora = relogio.get();
        List<AvaliacaoDaTurma> avaliacoes = repositorio.avaliacoesDaTurma(turma.id());
        avaliacoes.forEach(avaliacao -> correcaoAutomatica.accept(avaliacao.id()));
        List<AlunoDaTurma> alunos = repositorio.alunosDaTurma(turma.id());

        Map<String, TentativaRegistrada> tentativas = new HashMap<>();
        for (TentativaRegistrada t : repositorio.tentativasDaTurma(turma.id())) {
            tentativas.put(chave(t.idAluno(), t.idAvaliacao()), t);
        }

        List<MediaFinal> medias = new ArrayList<>();
        Map<Integer, Map<Integer, NotaDoAluno>> notasPorAluno = new HashMap<>();
        for (AlunoDaTurma aluno : alunos) {
            Map<Integer, NotaDoAluno> notas = new LinkedHashMap<>();
            List<NotaPonderada> ponderadas = new ArrayList<>();
            for (AvaliacaoDaTurma avaliacao : avaliacoes) {
                boolean encerrada = avaliacao.encerradaEm(agora);
                TentativaRegistrada t = tentativas.get(chave(aluno.id(), avaliacao.id()));
                if (t == null && !encerrada) {
                    ponderadas.add(new NotaPonderada(avaliacao.id(), avaliacao.titulo(), avaliacao.peso(), false,
                            NotaDoAluno.Situacao.NAO_REALIZADA, BigDecimal.ZERO));
                    continue;
                }
                NotaDoAluno nota = t == null
                        ? NotaDoAluno.de(avaliacao.id(), avaliacao.titulo(), avaliacao.prazo(), null, null, false, false)
                        : NotaDoAluno.de(avaliacao.id(), avaliacao.titulo(), avaliacao.prazo(), t.status(),
                        t.notaTotal(), t.corrigidaCompletamente(), t.correcaoFinalizada());
                notas.put(avaliacao.id(), nota);
                ponderadas.add(NotaPonderada.de(nota, avaliacao.peso(), encerrada));
            }
            medias.add(calculo.calcular(aluno.id(), aluno.nome(), ponderadas));
            notasPorAluno.put(aluno.id(), notas);
        }

        repositorio.sincronizar(turma.id(), medias.stream().filter(MediaFinal::estaPronta).toList());
        Map<Integer, Boolean> liberacao = repositorio.liberacaoPorAluno(turma.id());

        List<LinhaDoQuadro> linhas = new ArrayList<>();
        for (int i = 0; i < alunos.size(); i++) {
            AlunoDaTurma aluno = alunos.get(i);
            linhas.add(new LinhaDoQuadro(aluno, notasPorAluno.get(aluno.id()), medias.get(i),
                    liberacao.getOrDefault(aluno.id(), false)));
        }
        return new QuadroDaTurma(turma, avaliacoes, linhas, calculo.podeLiberar(medias));
    }

    public QuadroDaTurma liberar(TurmaResumo turma) {
        QuadroDaTurma quadro = montarQuadro(turma);
        if (!quadro.podeLiberar()) {
            throw new IllegalStateException(quadro.linhas().isEmpty()
                    ? "A turma não tem alunos matriculados."
                    : "Ainda há médias pendentes. Corrija todas as avaliações encerradas antes de liberar.");
        }
        repositorio.liberar(turma.id());
        return montarQuadro(turma);
    }

    public void recalcularPelaAvaliacao(int idAvaliacao) {
        Integer idTurma = repositorio.turmaDaAvaliacao(idAvaliacao);
        if (idTurma != null) {
            montarQuadro(new TurmaResumo(idTurma, ""));
        }
    }

    public List<MediaLiberada> mediasLiberadasDoAluno(String email) {
        return repositorio.mediasLiberadasDoAluno(email);
    }

    private static String chave(int idAluno, int idAvaliacao) {
        return idAluno + ":" + idAvaliacao;
    }
}
