package br.snctech.Repositorio;

import br.snctech.Questao;
import br.snctech.model.Tentativa;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Guarda as tentativas só enquanto a aplicação está aberta. Usado nos testes e no modo memória. */
public class RepositorioTentativasMemoria implements RepositorioTentativas {
    private final List<Tentativa> tentativas = new ArrayList<>();
    private int proximoId = 1;

    @Override
    public Tentativa salvar(int avaliacaoId, String aluno, LocalDateTime inicio, LocalDateTime limite) {
        Tentativa nova = new Tentativa(proximoId++, avaliacaoId, aluno, inicio, limite);
        tentativas.add(nova);
        return nova;
    }

    @Override
    public Tentativa buscarPorId(int id) {
        return tentativas.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
    }

    @Override
    public Tentativa buscarPorAvaliacaoEAluno(int avaliacaoId, String aluno) {
        return tentativas.stream()
                .filter(t -> t.getAvaliacaoId() == avaliacaoId && t.getAluno().equalsIgnoreCase(aluno.trim()))
                .findFirst().orElse(null);
    }

    @Override
    public List<Tentativa> listarPorAvaliacao(int avaliacaoId) {
        return tentativas.stream().filter(t -> t.getAvaliacaoId() == avaliacaoId).toList();
    }

    @Override
    public void registrarResposta(Tentativa tentativa, Questao questao, String resposta) {
        tentativa.registrarResposta(questao, resposta);
    }

    @Override
    public void concluir(Tentativa tentativa, Tentativa.Status status, LocalDateTime momento) {
        tentativa.concluir(status, momento);
    }
}
