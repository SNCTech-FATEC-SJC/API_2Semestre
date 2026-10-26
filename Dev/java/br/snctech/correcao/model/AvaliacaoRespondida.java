package br.snctech.correcao.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Prova respondida por um aluno (no banco: tabela tentativas_avaliacao). */
public class AvaliacaoRespondida {

    private final Integer id;
    private final Integer idAvaliacao;
    private final Integer idAluno;
    private final Map<Integer, RespostaQuestao> respostas = new LinkedHashMap<>();

    public AvaliacaoRespondida(Integer id, Integer idAvaliacao, Integer idAluno) {
        this.id = id;
        this.idAvaliacao = idAvaliacao;
        this.idAluno = idAluno;
    }

    public void adicionar(RespostaQuestao resposta) {
        respostas.put(resposta.getIdQuestao(), resposta);
    }

    public Optional<RespostaQuestao> resposta(Integer idQuestao) {
        return Optional.ofNullable(respostas.get(idQuestao));
    }

    /** Se o aluno deixou a questão em branco, cria uma resposta vazia para ela poder ser corrigida (nota 0). */
    public RespostaQuestao respostaOuVazia(Integer idQuestao) {
        return respostas.computeIfAbsent(idQuestao, RespostaQuestao::new);
    }

    public Map<Integer, RespostaQuestao> respostas() {
        return Collections.unmodifiableMap(respostas);
    }

    /** Ainda existe questão sem correção ou aguardando o professor. */
    public boolean temPendencias() {
        return respostas.values().stream()
                .anyMatch(r -> r.getStatus() == null || r.getStatus() == StatusCorrecao.PENDENTE_MANUAL);
    }

    /** Soma das notas já lançadas (ignora pendentes). */
    public BigDecimal notaTotal() {
        return respostas.values().stream()
                .map(RespostaQuestao::getNota)
                .filter(n -> n != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** A nota só vira "nota final" quando não há mais nada pendente. */
    public boolean notaFinalConfirmada() {
        return !respostas.isEmpty() && !temPendencias();
    }

    public Integer getId() { return id; }
    public Integer getIdAvaliacao() { return idAvaliacao; }
    public Integer getIdAluno() { return idAluno; }
}
