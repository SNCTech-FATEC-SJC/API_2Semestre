package br.snctech.service;

import br.snctech.Questao;
import br.snctech.model.Avaliacao;
import br.snctech.Repositorio.RepositorioAvaliacoes;
import java.util.List;
import java.time.LocalDate;

/** Centraliza as regras de negócio de criação e edição de avaliações. */
public class AvaliacaoService {
    private final RepositorioAvaliacoes repositorio;
    public AvaliacaoService(RepositorioAvaliacoes repositorio) { this.repositorio = repositorio; }

    public Avaliacao criar(String nome, List<Questao> questoes) {
        return criar(nome, questoes, "", null, null);
    }

    public Avaliacao criar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo) {
        validar(nome, questoes, turma, data, prazo);
        return repositorio.salvar(nome.trim(), questoes, turma.trim(), data, prazo);
    }

    public Avaliacao editar(int id, String nome, List<Questao> questoes) {
        Avaliacao existente = repositorio.buscarPorId(id);
        return editar(id, nome, questoes, existente == null ? "" : existente.getTurma(),
                existente == null ? null : existente.getData(), existente == null ? null : existente.getPrazo());
    }

    public Avaliacao editar(int id, String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo) {
        validar(nome, questoes, turma, data, prazo);
        Avaliacao existente = repositorio.buscarPorId(id);
        if (existente == null) throw new IllegalArgumentException("A avaliação não foi encontrada.");

        // Uma avaliação respondida nunca é alterada: criamos uma nova versão.
        if (existente.isRespondida()) {
            String nomeNovo = nome.trim();
            if (existente.getNome().equalsIgnoreCase(nomeNovo)) {
                String base = nomeNovo;
                int versao = 2;
                do {
                    nomeNovo = base + versao++;
                } while (nomeJaExiste(nomeNovo));
            }
            return repositorio.salvar(nomeNovo, questoes, turma.trim(), data, prazo);
        }
        repositorio.atualizar(existente, nome.trim(), questoes, turma.trim(), data, prazo);
        return existente;
    }

    private boolean nomeJaExiste(String nome) {
        return repositorio.listarTodas().stream().anyMatch(a -> a.getNome().equalsIgnoreCase(nome));
    }

    private void validar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Defina um nome para a avaliação.");
        if (questoes == null || questoes.isEmpty()) throw new IllegalArgumentException("Selecione pelo menos uma questão.");
        if (turma == null || turma.isBlank()) throw new IllegalArgumentException("Informe a turma da avaliação.");
        if (data == null) throw new IllegalArgumentException("Selecione a data da avaliação.");
        if (prazo == null) throw new IllegalArgumentException("Selecione o prazo da avaliação.");
        if (prazo.isBefore(data)) throw new IllegalArgumentException("O prazo não pode ser anterior à data da avaliação.");
    }
}
