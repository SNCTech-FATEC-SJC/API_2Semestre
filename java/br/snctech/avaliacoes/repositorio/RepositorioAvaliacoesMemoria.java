package br.snctech.avaliacoes.repositorio;

import br.snctech.questoes.model.Questao;
import br.snctech.avaliacoes.model.Avaliacao;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Guarda as avaliações só enquanto a aplicação está aberta. Usado nos testes e no modo memória. */
public class RepositorioAvaliacoesMemoria implements RepositorioAvaliacoes {
    private final List<Avaliacao> avaliacoes = new ArrayList<>();
    private int proximoId = 1;

    public Avaliacao salvar(String nome, List<Questao> questoes) {
        Avaliacao nova = new Avaliacao(proximoId++, nome, questoes);
        avaliacoes.add(nova);
        return nova;
    }

    @Override
    public Avaliacao salvar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo) {
        Avaliacao nova = salvar(nome, questoes);
        nova.setTurma(turma);
        nova.setData(data);
        nova.setPrazo(prazo);
        return nova;
    }

    @Override
    public Avaliacao salvar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                            LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos) {
        Avaliacao nova = salvar(nome, questoes, turma, data, prazo);
        nova.setHoraInicio(horaInicio);
        nova.setHoraFim(horaFim);
        nova.setDuracaoMinutos(duracaoMinutos);
        return nova;
    }

    @Override
    public Avaliacao salvarNovaVersao(Avaliacao original, String nome, List<Questao> questoes, String turma,
                                      LocalDate data, LocalDate prazo, LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos) {
        Avaliacao nova = salvar(nome, questoes, turma, data, prazo, horaInicio, horaFim, duracaoMinutos);
        // O "pai" é sempre a avaliação ORIGINAL (a raiz), mesmo quando se edita uma versão 2, 3...
        // Assim memória e banco (JDBC) numeram as versões do mesmo jeito.
        int raizId = original.getAvaliacaoPaiId() == null ? original.getId() : original.getAvaliacaoPaiId();
        int maiorVersao = original.getVersao();
        for (Avaliacao outra : avaliacoes) {
            if (outra.getAvaliacaoPaiId() != null && outra.getAvaliacaoPaiId() == raizId) {
                maiorVersao = Math.max(maiorVersao, outra.getVersao());
            }
        }
        nova.setAvaliacaoPaiId(raizId);
        nova.setVersao(maiorVersao + 1);
        return nova;
    }

    @Override
    public List<Avaliacao> listarTodas() { return new ArrayList<>(avaliacoes); }

    @Override
    public Avaliacao buscarPorId(int id) {
        return avaliacoes.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
    }

    public void atualizar(Avaliacao avaliacao, String nome, List<Questao> questoes,
                          String turma, LocalDate data, LocalDate prazo) {
        avaliacao.setNome(nome);
        avaliacao.setQuestoes(questoes);
        avaliacao.setTurma(turma);
        avaliacao.setData(data);
        avaliacao.setPrazo(prazo);
    }

    @Override
    public void atualizar(Avaliacao avaliacao, String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                          LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos) {
        atualizar(avaliacao, nome, questoes, turma, data, prazo);
        avaliacao.setHoraInicio(horaInicio);
        avaliacao.setHoraFim(horaFim);
        avaliacao.setDuracaoMinutos(duracaoMinutos);
    }

    @Override
    public void atualizarPeso(Avaliacao avaliacao, double peso) {
        avaliacao.setPeso(peso);
    }

    @Override
    public void atualizarDisponibilizacao(Avaliacao avaliacao, boolean disponibilizada) {
        avaliacao.setDisponibilizada(disponibilizada);
    }
}
