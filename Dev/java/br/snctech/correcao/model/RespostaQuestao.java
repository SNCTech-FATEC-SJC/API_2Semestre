package br.snctech.correcao.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Resposta do aluno a UMA questão + o resultado da correção dela.
 * No banco corresponde a uma linha de respostas_aluno.
 */
public class RespostaQuestao {

    private final Integer idQuestao;
    private String alternativaId;                  // múltipla escolha
    private Map<String, String> pares = new HashMap<>(); // associação
    private String texto;                          // discursiva
    private BigDecimal nota;
    private BigDecimal notaSugerida;               // pré-correção da discursiva
    private StatusCorrecao status;
    private Integer corrigidoPor;                     // id do professor (correção manual)

    public RespostaQuestao(Integer idQuestao) {
        this.idQuestao = idQuestao;
    }

    public void aplicar(ResultadoCorrecao resultado) {
        this.nota = resultado.nota();
        this.status = resultado.status();
        this.notaSugerida = resultado.notaSugerida();
    }

    public Integer getIdQuestao() { return idQuestao; }
    public String getAlternativaId() { return alternativaId; }
    public void setAlternativaId(String alternativaId) { this.alternativaId = alternativaId; }
    public Map<String, String> getPares() { return pares; }
    public void setPares(Map<String, String> pares) { this.pares = pares == null ? new HashMap<>() : new HashMap<>(pares); }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public BigDecimal getNota() { return nota; }
    public void setNota(BigDecimal nota) { this.nota = nota; }
    public BigDecimal getNotaSugerida() { return notaSugerida; }
    public void setNotaSugerida(BigDecimal notaSugerida) { this.notaSugerida = notaSugerida; }
    public StatusCorrecao getStatus() { return status; }
    public void setStatus(StatusCorrecao status) { this.status = status; }
    public Integer getCorrigidoPor() { return corrigidoPor; }
    public void setCorrigidoPor(Integer corrigidoPor) { this.corrigidoPor = corrigidoPor; }
}
