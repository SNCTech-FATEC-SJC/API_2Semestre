package br.snctech.avaliacoes.service;

import br.snctech.questoes.model.Questao;
import br.snctech.avaliacoes.model.Avaliacao;
import br.snctech.avaliacoes.repositorio.RepositorioAvaliacoes;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

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

    /** Compatibilidade com o campo legado de duração; a realização usa somente o prazo geral. */
    public Avaliacao criar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                           LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos) {
        return criar(nome, questoes, turma, data, prazo, horaInicio, horaFim, duracaoMinutos, 1.0);
    }

    public Avaliacao criar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                           LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos, double peso) {
        validar(nome, questoes, turma, data, prazo);
        validarHorarios(data, prazo, horaInicio, horaFim, duracaoMinutos);
        validarPeso(peso);
        return repositorio.salvar(nome.trim(), questoes, turma.trim(), data, prazo, horaInicio, horaFim, duracaoMinutos, peso);
    }

    public Avaliacao editar(int id, String nome, List<Questao> questoes) {
        Avaliacao existente = repositorio.buscarPorId(id);
        return editar(id, nome, questoes, existente == null ? "" : existente.getTurma(),
                existente == null ? null : existente.getData(), existente == null ? null : existente.getPrazo());
    }

    public Avaliacao editar(int id, String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo) {
        validar(nome, questoes, turma, data, prazo);
        Avaliacao existente = buscarExistente(id);
        // Sem os novos campos, a avaliação mantém o horário e a duração que já tinha
        // (inclusive na nova versão criada quando ela já foi respondida).
        return aplicarEdicao(existente, nome, questoes, turma, data, prazo,
                existente.getHoraInicio(), existente.getHoraFim(), existente.getDuracaoMinutos(), existente.getPeso());
    }

    public Avaliacao editar(int id, String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                            LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos) {
        Avaliacao existente = buscarExistente(id);
        return editar(id, nome, questoes, turma, data, prazo, horaInicio, horaFim, duracaoMinutos, existente.getPeso());
    }

    public Avaliacao editar(int id, String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                            LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos, double peso) {
        validar(nome, questoes, turma, data, prazo);
        validarHorarios(data, prazo, horaInicio, horaFim, duracaoMinutos);
        validarPeso(peso);
        Avaliacao existente = buscarExistente(id);
        return aplicarEdicao(existente, nome, questoes, turma, data, prazo, horaInicio, horaFim, duracaoMinutos, peso);
    }

    private Avaliacao buscarExistente(int id) {
        Avaliacao existente = repositorio.buscarPorId(id);
        if (existente == null) throw new IllegalArgumentException("A avaliação não foi encontrada.");
        return existente;
    }

    private Avaliacao aplicarEdicao(Avaliacao existente, String nome, List<Questao> questoes, String turma,
                                    LocalDate data, LocalDate prazo,
                                    LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos, double peso) {
        if (existente.isDisponibilizada()) {
            throw new IllegalArgumentException("Esta avaliação está ativa e não pode ser editada.");
        }
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
            Avaliacao nova = repositorio.salvarNovaVersao(existente, nomeNovo, questoes, turma.trim(),
                    data, prazo, horaInicio, horaFim, duracaoMinutos, peso);
            nova.setRespondida(false);
            return nova;
        }
        repositorio.atualizar(existente, nome.trim(), questoes, turma.trim(), data, prazo, horaInicio, horaFim, duracaoMinutos, peso);
        return existente;
    }

    private boolean nomeJaExiste(String nome) {
        return repositorio.listarTodas().stream().anyMatch(a -> a.getNome().equalsIgnoreCase(nome));
    }

    private void validarPeso(double peso) {
        if (!Double.isFinite(peso) || peso <= 0 || peso > 999.99) {
            throw new IllegalArgumentException("O peso deve ser maior que zero e não pode ultrapassar 999,99.");
        }
    }

    private void validar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Defina um nome para a avaliação.");
        if (questoes == null || questoes.isEmpty()) throw new IllegalArgumentException("Selecione pelo menos uma questão.");
        if (turma == null || turma.isBlank()) throw new IllegalArgumentException("Informe a turma da avaliação.");
        if (data == null) throw new IllegalArgumentException("Selecione a data da avaliação.");
        if (prazo == null) throw new IllegalArgumentException("Selecione o prazo da avaliação.");
        if (prazo.isBefore(data)) throw new IllegalArgumentException("O prazo não pode ser anterior à data da avaliação.");
    }

    /** Só vale para quem informa horário e duração: o término precisa vir depois do início. */
    private void validarHorarios(LocalDate data, LocalDate prazo, LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos) {
        if (horaInicio == null) throw new IllegalArgumentException("Informe a hora de início da avaliação.");
        if (horaFim == null) throw new IllegalArgumentException("Informe a hora de término da avaliação.");
        if (!prazo.atTime(horaFim).isAfter(data.atTime(horaInicio))) {
            throw new IllegalArgumentException("O término da avaliação deve ser depois do início.");
        }
        if (duracaoMinutos < 0) throw new IllegalArgumentException("A duração não pode ser negativa.");
    }
}
