package br.snctech.correcao.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class AvaliacaoRespondida {
    private final Integer id;
    private final Integer idAvaliacao;
    private final Integer idAluno;
    private final Map<Integer, RespostaQuestao> respostas = new LinkedHashMap<>();
    private boolean correcaoFinalizada;

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

    public RespostaQuestao respostaOuVazia(Integer idQuestao) {
        return respostas.computeIfAbsent(idQuestao, RespostaQuestao::new);
    }

    public Map<Integer, RespostaQuestao> respostas() {
        return Collections.unmodifiableMap(respostas);
    }

    public boolean temPendencias() {
        return respostas.values().stream()
                .anyMatch(r -> r.getStatus() == null || r.getStatus() == StatusCorrecao.PENDENTE_MANUAL);
    }

    public BigDecimal notaTotal() {
        return respostas.values().stream()
                .map(RespostaQuestao::getNota)
                .filter(n -> n != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean notaFinalConfirmada() {
        return !respostas.isEmpty() && !temPendencias();
    }

    public boolean isCorrecaoFinalizada() {
        return correcaoFinalizada;
    }

    public void setCorrecaoFinalizada(boolean correcaoFinalizada) {
        this.correcaoFinalizada = correcaoFinalizada;
    }

    public void finalizarCorrecao() {
        if (!notaFinalConfirmada()) {
            throw new IllegalStateException("Ainda há questões sem nota. Corrija todas antes de finalizar a correção.");
        }
        correcaoFinalizada = true;
    }

    public Integer getId() { return id; }
    public Integer getIdAvaliacao() { return idAvaliacao; }
    public Integer getIdAluno() { return idAluno; }
}
