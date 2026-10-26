package br.snctech.model;

import br.snctech.Questao;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registro da realização de uma avaliação por um aluno.
 * Existe no máximo UMA tentativa por aluno em cada avaliação: ela é criada
 * no instante em que o aluno abre a prova e não pode ser reaberta depois.
 * Quem altera o estado é o TentativaService; as telas só leem.
 */
public class Tentativa {

    /** EM_ANDAMENTO: aluno respondendo. FINALIZADA: aluno entregou.
     *  ENCERRADA: o tempo acabou ou o aluno fechou a prova sem entregar. */
    public enum Status { EM_ANDAMENTO, FINALIZADA, ENCERRADA }

    private final int id;
    private final int avaliacaoId;
    private final String aluno;          // identificação provisória (ainda não há banco de alunos)
    private final LocalDateTime inicio;  // quando o aluno abriu a prova
    private final LocalDateTime limite;  // quando o tempo acaba (duração ou prazo, o que vier primeiro)
    private LocalDateTime fim;           // quando terminou (null enquanto em andamento)
    private Status status;
    // Resposta de cada questão, sempre como texto:
    //  Múltipla Escolha -> texto da alternativa; Verdadeiro/Falso -> "Verdadeiro" ou "Falso"; Dissertativa -> o texto digitado.
    private final Map<Questao, String> respostas = new LinkedHashMap<>();

    public Tentativa(int id, int avaliacaoId, String aluno, LocalDateTime inicio, LocalDateTime limite) {
        this.id = id;
        this.avaliacaoId = avaliacaoId;
        this.aluno = aluno;
        this.inicio = inicio;
        this.limite = limite;
        this.fim = null;
        this.status = Status.EM_ANDAMENTO;
    }

    public int getId() { return id; }
    public int getAvaliacaoId() { return avaliacaoId; }
    public String getAluno() { return aluno; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getLimite() { return limite; }
    public LocalDateTime getFim() { return fim; }
    public Status getStatus() { return status; }
    public boolean isEmAndamento() { return status == Status.EM_ANDAMENTO; }

    /** Cópia das respostas, na ordem em que foram registradas. */
    public Map<Questao, String> getRespostas() { return new LinkedHashMap<>(respostas); }
    public String getResposta(Questao questao) { return respostas.get(questao); }

    /** Uso interno do service. Resposta vazia apaga a resposta anterior da questão. */
    public void registrarResposta(Questao questao, String resposta) {
        if (resposta == null || resposta.isBlank()) respostas.remove(questao);
        else respostas.put(questao, resposta);
    }

    /** Uso interno do service. */
    public void concluir(Status novoStatus, LocalDateTime momento) {
        this.status = novoStatus;
        this.fim = momento;
    }
}
