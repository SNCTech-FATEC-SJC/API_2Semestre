package br.snctech.service;

import br.snctech.Questao;
import br.snctech.Repositorio.RepositorioAvaliacoes;
import br.snctech.Repositorio.RepositorioTentativas;
import br.snctech.model.Avaliacao;
import br.snctech.model.Tentativa;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Regras da realização da avaliação pelo aluno (PB-06 e PB-07).
 * As telas chamam estes métodos e mostram a mensagem de IllegalArgumentException
 * quando uma regra é violada, do mesmo jeito que já é feito com o AvaliacaoService.
 */
public class TentativaService {
    private final RepositorioTentativas tentativas;
    private final RepositorioAvaliacoes avaliacoes;
    private final Clock relogio; // permite testar com datas fixas

    public TentativaService(RepositorioTentativas tentativas, RepositorioAvaliacoes avaliacoes) {
        this(tentativas, avaliacoes, Clock.systemDefaultZone());
    }

    public TentativaService(RepositorioTentativas tentativas, RepositorioAvaliacoes avaliacoes, Clock relogio) {
        this.tentativas = tentativas;
        this.avaliacoes = avaliacoes;
        this.relogio = relogio;
    }

    /** Devolve null se o aluno pode abrir a prova agora; senão, o motivo (para mostrar na tela). */
    public String motivoDeBloqueio(int avaliacaoId, String aluno) {
        Avaliacao avaliacao = avaliacoes.buscarPorId(avaliacaoId);
        if (avaliacao == null) return "A avaliação não foi encontrada.";
        if (aluno == null || aluno.isBlank()) return "Informe o e-mail cadastrado do aluno.";
        if (!tentativas.alunoExiste(aluno)) return "Não encontramos um aluno cadastrado com esse e-mail.";
        if (!avaliacao.isDisponibilizada()) return "A avaliação ainda não foi disponibilizada.";

        LocalDateTime inicio = inicioDa(avaliacao);
        LocalDateTime fim = fimDa(avaliacao);
        if (inicio == null || fim == null) return "A avaliação não tem início e prazo definidos.";

        LocalDateTime agora = agora();
        if (agora.isBefore(inicio)) return "A avaliação ainda não começou.";
        if (!agora.isBefore(fim)) return "O prazo da avaliação terminou.";
        if (tentativas.buscarPorAvaliacaoEAluno(avaliacaoId, aluno) != null) {
            return "Você já abriu esta avaliação e não pode abri-la novamente.";
        }
        return null;
    }

    /** Registra a abertura da prova. A partir daqui o aluno não consegue abri-la de novo. */
    public Tentativa iniciar(int avaliacaoId, String aluno) {
        String motivo = motivoDeBloqueio(avaliacaoId, aluno);
        if (motivo != null) throw new IllegalArgumentException(motivo);

        Avaliacao avaliacao = avaliacoes.buscarPorId(avaliacaoId);
        LocalDateTime agora = agora();

        // O tempo acaba no fim da duração ou no prazo, o que vier primeiro.
        LocalDateTime limite = fimDa(avaliacao);
        if (avaliacao.getDuracaoMinutos() > 0) {
            LocalDateTime porDuracao = agora.plusMinutes(avaliacao.getDuracaoMinutos());
            if (porDuracao.isBefore(limite)) limite = porDuracao;
        }

        Tentativa tentativa = tentativas.salvar(avaliacaoId, aluno.trim(), agora, limite);
        // Com um aluno na prova, a avaliação passa a contar como respondida:
        // editar cria uma nova versão e não altera esta (regra da PB-01).
        avaliacao.setRespondida(true);
        return tentativa;
    }

    /** Registra (ou troca) a resposta de uma questão. Resposta vazia apaga a anterior. */
    public void responder(int tentativaId, Questao questao, String resposta) {
        Tentativa tentativa = buscar(tentativaId);
        encerrarSeTempoAcabou(tentativa);
        if (!tentativa.isEmAndamento()) {
            throw new IllegalArgumentException(tentativa.getStatus() == Tentativa.Status.FINALIZADA
                    ? "Esta avaliação já foi finalizada."
                    : "O tempo da avaliação acabou ou ela já foi encerrada.");
        }
        Avaliacao avaliacao = avaliacoes.buscarPorId(tentativa.getAvaliacaoId());
        if (avaliacao == null || !avaliacao.getQuestoes().contains(questao)) {
            throw new IllegalArgumentException("Esta questão não pertence à avaliação.");
        }
        tentativas.registrarResposta(tentativa, questao, resposta);
    }

    /** O aluno clicou em entregar. Se o tempo já tinha acabado, fica ENCERRADA com as respostas já registradas. */
    public Tentativa finalizar(int tentativaId) {
        Tentativa tentativa = buscar(tentativaId);
        encerrarSeTempoAcabou(tentativa);
        if (tentativa.isEmAndamento()) tentativas.concluir(tentativa, Tentativa.Status.FINALIZADA, agora());
        return tentativa;
    }

    /** O aluno fechou a prova (ou o cronômetro chegou a zero) sem entregar. Mantém as respostas já registradas. */
    public Tentativa encerrar(int tentativaId) {
        Tentativa tentativa = buscar(tentativaId);
        if (tentativa.isEmAndamento()) {
            LocalDateTime agora = agora();
            tentativas.concluir(tentativa, Tentativa.Status.ENCERRADA, agora.isBefore(tentativa.getLimite()) ? agora : tentativa.getLimite());
        }
        return tentativa;
    }

    /** Segundos que faltam para o tempo acabar (0 se a tentativa não está em andamento). Para o cronômetro da tela. */
    public long segundosRestantes(int tentativaId) {
        Tentativa tentativa = buscar(tentativaId);
        encerrarSeTempoAcabou(tentativa);
        if (!tentativa.isEmAndamento()) return 0;
        return Duration.between(agora(), tentativa.getLimite()).getSeconds();
    }

    private void encerrarSeTempoAcabou(Tentativa tentativa) {
        if (tentativa.isEmAndamento() && !agora().isBefore(tentativa.getLimite())) {
            tentativas.concluir(tentativa, Tentativa.Status.ENCERRADA, tentativa.getLimite());
        }
    }

    private Tentativa buscar(int tentativaId) {
        Tentativa tentativa = tentativas.buscarPorId(tentativaId);
        if (tentativa == null) throw new IllegalArgumentException("A tentativa não foi encontrada.");
        return tentativa;
    }

    private LocalDateTime agora() { return LocalDateTime.now(relogio); }

    private LocalDateTime inicioDa(Avaliacao a) {
        return (a.getData() == null || a.getHoraInicio() == null) ? null : a.getData().atTime(a.getHoraInicio());
    }

    private LocalDateTime fimDa(Avaliacao a) {
        return (a.getPrazo() == null || a.getHoraFim() == null) ? null : a.getPrazo().atTime(a.getHoraFim());
    }
}
