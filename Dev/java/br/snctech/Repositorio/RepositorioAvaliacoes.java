package br.snctech.Repositorio;

import br.snctech.Questao;
import br.snctech.model.Avaliacao;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

/** Repositório temporário em memória. Poderá ser substituído por acesso ao BD. */
public class RepositorioAvaliacoes {
    private final List<Avaliacao> avaliacoes = new ArrayList<>();
    private int proximoId = 1;

    public Avaliacao salvar(String nome, List<Questao> questoes) {
        Avaliacao nova = new Avaliacao(proximoId++, nome, questoes);
        avaliacoes.add(nova);
        return nova;
    }

    public Avaliacao salvar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo) {
        Avaliacao nova = salvar(nome, questoes);
        nova.setTurma(turma);
        nova.setData(data);
        nova.setPrazo(prazo);
        return nova;
    }

    public List<Avaliacao> listarTodas() { return new ArrayList<>(avaliacoes); }

    public Avaliacao buscarPorId(int id) {
        return avaliacoes.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
    }

    public void atualizar(Avaliacao avaliacao, String nome, List<Questao> questoes) {
        atualizar(avaliacao, nome, questoes, avaliacao.getTurma(), avaliacao.getData(), avaliacao.getPrazo());
    }

    public void atualizar(Avaliacao avaliacao, String nome, List<Questao> questoes,
                          String turma, LocalDate data, LocalDate prazo) {
        avaliacao.setNome(nome);
        avaliacao.setQuestoes(questoes);
        avaliacao.setTurma(turma);
        avaliacao.setData(data);
        avaliacao.setPrazo(prazo);
    }
}
