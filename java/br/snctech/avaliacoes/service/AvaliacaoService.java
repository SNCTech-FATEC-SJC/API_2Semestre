package br.snctech.avaliacoes.service;

import br.snctech.questoes.model.Questao;
import br.snctech.avaliacoes.model.Avaliacao;
import br.snctech.avaliacoes.repositorio.RepositorioAvaliacoes;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

/** Centraliza as regras de negócio de criação e edição de avaliações. */
public class AvaliacaoService {
    /** Peso usado quando o professor não informa nenhum (igual ao DEFAULT da coluna avaliacoes.peso). */
    public static final double PESO_PADRAO = 1.0;
    /** Maior peso que cabe na coluna avaliacoes.peso, que é numeric(5,2). */
    public static final double PESO_MAXIMO = 999.99;

    private final RepositorioAvaliacoes repositorio;
    public AvaliacaoService(RepositorioAvaliacoes repositorio) { this.repositorio = repositorio; }

    public Avaliacao criar(String nome, List<Questao> questoes) {
        return criar(nome, questoes, "", null, null);
    }

    public Avaliacao criar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo) {
        validar(nome, questoes, turma, data, prazo);
        return repositorio.salvar(nome.trim(), questoes, turma.trim(), data, prazo);
    }

    /** Cria a avaliação já com o horário de início/término e a duração por aluno (0 = só vale o prazo). */
    public Avaliacao criar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                           LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos) {
        validar(nome, questoes, turma, data, prazo);
        validarHorarios(data, prazo, horaInicio, horaFim, duracaoMinutos);
        return repositorio.salvar(nome.trim(), questoes, turma.trim(), data, prazo, horaInicio, horaFim, duracaoMinutos);
    }

    /** Cria a avaliação já com o peso definido pelo professor (PB-01). O peso é conferido ANTES de salvar. */
    public Avaliacao criar(String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                           LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos, double peso) {
        double pesoValido = validarPeso(peso);
        Avaliacao nova = criar(nome, questoes, turma, data, prazo, horaInicio, horaFim, duracaoMinutos);
        if (nova.getPeso() != pesoValido) repositorio.atualizarPeso(nova, pesoValido);
        return nova;
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
        validar(nome, questoes, turma, data, prazo);
        validarHorarios(data, prazo, horaInicio, horaFim, duracaoMinutos);
        Avaliacao existente = buscarExistente(id);
        // Sem o campo peso, a avaliação mantém o peso que já tinha.
        return aplicarEdicao(existente, nome, questoes, turma, data, prazo, horaInicio, horaFim, duracaoMinutos,
                existente.getPeso());
    }

    /** Edita a avaliação e também define o novo peso. Se ela já foi respondida, o peso novo vale só na nova versão. */
    public Avaliacao editar(int id, String nome, List<Questao> questoes, String turma, LocalDate data, LocalDate prazo,
                            LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos, double peso) {
        double pesoValido = validarPeso(peso);
        validar(nome, questoes, turma, data, prazo);
        validarHorarios(data, prazo, horaInicio, horaFim, duracaoMinutos);
        Avaliacao existente = buscarExistente(id);
        return aplicarEdicao(existente, nome, questoes, turma, data, prazo, horaInicio, horaFim, duracaoMinutos, pesoValido);
    }

    /**
     * Altera só o peso de uma avaliação que já existe.
     * Avaliação sem respostas: o peso é trocado nela mesma.
     * Avaliação já respondida: ela NUNCA é alterada (as notas dos alunos dependem do peso antigo), então é criada
     * uma nova versão igual à original, só com o peso novo (o nome ganha o número no fim, ex.: "Prova" -> "Prova2").
     */
    public Avaliacao alterarPeso(int id, double peso) {
        double pesoValido = validarPeso(peso);
        Avaliacao existente = buscarExistente(id);
        if (!existente.isRespondida()) {
            if (existente.getPeso() != pesoValido) repositorio.atualizarPeso(existente, pesoValido);
            return existente;
        }
        validar(existente.getNome(), existente.getQuestoes(), existente.getTurma(), existente.getData(), existente.getPrazo());
        return aplicarEdicao(existente, existente.getNome(), existente.getQuestoes(), existente.getTurma(),
                existente.getData(), existente.getPrazo(), existente.getHoraInicio(), existente.getHoraFim(),
                existente.getDuracaoMinutos(), pesoValido);
    }

    private Avaliacao buscarExistente(int id) {
        Avaliacao existente = repositorio.buscarPorId(id);
        if (existente == null) throw new IllegalArgumentException("A avaliação não foi encontrada.");
        return existente;
    }

    private Avaliacao aplicarEdicao(Avaliacao existente, String nome, List<Questao> questoes, String turma,
                                    LocalDate data, LocalDate prazo,
                                    LocalTime horaInicio, LocalTime horaFim, int duracaoMinutos, double peso) {
        // Uma avaliação respondida nunca é alterada: criamos uma nova versão.
        if (existente.isRespondida()) {
            String nomeNovo = nome.trim();
            if (existente.getNome().equalsIgnoreCase(nomeNovo)) {
                // Mantendo o nome, o número vai no fim. Se estamos editando uma versão (ex.: "Avaliação2"),
                // a base é o nome da ORIGINAL, para não virar "Avaliação22".
                String base = nomeNovo;
                if (existente.getAvaliacaoPaiId() != null) {
                    Avaliacao raiz = repositorio.buscarPorId(existente.getAvaliacaoPaiId());
                    if (raiz != null) base = raiz.getNome();
                }
                int versao = 2;
                do {
                    nomeNovo = base + versao++;
                } while (nomeJaExiste(nomeNovo));
            }
            // salvarNovaVersao grava também o vínculo com a original (versao e avaliacao_pai_id).
            Avaliacao nova = repositorio.salvarNovaVersao(existente, nomeNovo, questoes, turma.trim(), data, prazo,
                    horaInicio, horaFim, duracaoMinutos);
            if (nova.getPeso() != peso) repositorio.atualizarPeso(nova, peso);
            return nova;
        }
        repositorio.atualizar(existente, nome.trim(), questoes, turma.trim(), data, prazo, horaInicio, horaFim, duracaoMinutos);
        if (existente.getPeso() != peso) repositorio.atualizarPeso(existente, peso);
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

    /** O peso precisa ser maior que zero e caber na coluna numeric(5,2). Devolve o valor com 2 casas decimais. */
    private double validarPeso(double peso) {
        if (Double.isNaN(peso) || Double.isInfinite(peso) || peso <= 0) {
            throw new IllegalArgumentException("O peso da avaliação deve ser maior que zero.");
        }
        double arredondado = Math.round(peso * 100.0) / 100.0;
        if (arredondado <= 0) throw new IllegalArgumentException("O peso da avaliação deve ser maior que zero.");
        if (arredondado > PESO_MAXIMO) throw new IllegalArgumentException("O peso da avaliação não pode ser maior que 999,99.");
        return arredondado;
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
