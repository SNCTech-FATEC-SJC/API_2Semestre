package br.snctech;

import br.snctech.Repositorio.RepositorioAvaliacoes;
import br.snctech.Repositorio.RepositorioAvaliacoesMemoria;
import br.snctech.Repositorio.RepositorioTentativas;
import br.snctech.Repositorio.RepositorioTentativasMemoria;
import br.snctech.model.Avaliacao;
import br.snctech.model.Tentativa;
import br.snctech.service.ApoioResposta;
import br.snctech.service.AvaliacaoService;
import br.snctech.service.TentativaService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Teste de console das regras de realização da avaliação (sem telas).
 * Rodar o main. Cada linha mostra OK ou FALHOU; no fim há o resumo.
 */
public class TesteRealizacao {

    /** Relógio que a gente controla, para simular a passagem do tempo. */
    static class RelogioFalso extends Clock {
        private Instant agora;
        private final ZoneId zona = ZoneId.systemDefault();
        RelogioFalso(LocalDateTime inicial) { irPara(inicial); }
        void irPara(LocalDateTime momento) { agora = momento.atZone(zona).toInstant(); }
        @Override public ZoneId getZone() { return zona; }
        @Override public Clock withZone(ZoneId z) { return this; }
        @Override public Instant instant() { return agora; }
    }

    static int ok = 0, falhas = 0;

    static void confere(String descricao, boolean condicao) {
        if (condicao) { ok++; System.out.println("OK      " + descricao); }
        else { falhas++; System.out.println("FALHOU  " + descricao); }
    }

    /** Confere que a ação lança IllegalArgumentException com a mensagem esperada. */
    static void exigeErro(String descricao, String trechoDaMensagem, Runnable acao) {
        try {
            acao.run();
            confere(descricao + " (deveria dar erro)", false);
        } catch (IllegalArgumentException e) {
            confere(descricao + " -> \"" + e.getMessage() + "\"", e.getMessage().contains(trechoDaMensagem));
        }
    }

    static LocalDateTime em(int dia, int hora, int minuto) { return LocalDateTime.of(2026, 10, dia, hora, minuto); }

    public static void main(String[] args) {
        Questao q1 = new Questao("Quanto é 2+2?", "Múltipla Escolha", null, List.of("3", "4", "5"), "4", null);
        Questao q2 = new Questao("A Terra é plana?", "Verdadeiro/Falso", null, null, "Falso", null);
        Questao q3 = new Questao("Explique MVC.", "Dissertativa", null, null, null, null);
        Questao outra = new Questao("Questão de outra prova", "Dissertativa", null, null, null, null);

        RepositorioAvaliacoes repoAval = new RepositorioAvaliacoesMemoria();
        RepositorioTentativas repoTent = new RepositorioTentativasMemoria();
        RelogioFalso relogio = new RelogioFalso(em(25, 9, 0));
        TentativaService service = new TentativaService(repoTent, repoAval, relogio);

        // Prova de 26/10 08:00 até 28/10 18:00, com 60 minutos por aluno
        Avaliacao prova = repoAval.salvar("Prova 1", List.of(q1, q2, q3), "1A",
                LocalDate.of(2026, 10, 26), LocalDate.of(2026, 10, 28));
        prova.setHoraInicio(LocalTime.of(8, 0));
        prova.setHoraFim(LocalTime.of(18, 0));
        prova.setDuracaoMinutos(60);

        System.out.println("--- Antes de abrir ---");
        exigeErro("Prova não disponibilizada", "não foi disponibilizada", () -> service.iniciar(prova.getId(), "ana"));
        prova.setDisponibilizada(true);
        exigeErro("Disponibilizada, mas antes do início", "ainda não começou", () -> service.iniciar(prova.getId(), "ana"));
        exigeErro("Aluno sem e-mail", "Informe o e-mail cadastrado do aluno.", () -> service.iniciar(prova.getId(), " "));
        exigeErro("Avaliação inexistente", "não foi encontrada", () -> service.iniciar(999, "ana"));

        System.out.println("--- Aluno responde e entrega ---");
        relogio.irPara(em(26, 10, 0));
        confere("Sem bloqueio dentro do período", service.motivoDeBloqueio(prova.getId(), "ana") == null);
        confere("Avaliação ainda não conta como respondida", !prova.isRespondida());
        Tentativa ana = service.iniciar(prova.getId(), "ana");
        confere("Abrir a prova marca a avaliação como respondida", prova.isRespondida());
        confere("Limite = início + 60 min", ana.getLimite().equals(em(26, 11, 0)));
        confere("Tentativa começa EM_ANDAMENTO", ana.getStatus() == Tentativa.Status.EM_ANDAMENTO);
        service.responder(ana.getId(), q1, "4");
        service.responder(ana.getId(), q2, "Verdadeiro");
        service.responder(ana.getId(), q2, "Falso");           // troca a resposta
        service.responder(ana.getId(), q3, "Separa dados, tela e lógica.");
        relogio.irPara(em(26, 10, 20));
        confere("Faltam 40 min (2400 s)", service.segundosRestantes(ana.getId()) == 2400);
        Tentativa entregue = service.finalizar(ana.getId());
        confere("Status FINALIZADA", entregue.getStatus() == Tentativa.Status.FINALIZADA);
        confere("Guardou as 3 respostas", entregue.getRespostas().size() == 3);
        confere("Resposta trocada vale a última", "Falso".equals(entregue.getResposta(q2)));
        exigeErro("Responder depois de entregar", "já foi finalizada", () -> service.responder(ana.getId(), q1, "3"));
        exigeErro("Ana tenta abrir de novo", "não pode abri-la novamente", () -> service.iniciar(prova.getId(), "ana"));

        System.out.println("--- Aluno fecha a prova sem entregar ---");
        Tentativa bia = service.iniciar(prova.getId(), "bia");
        service.responder(bia.getId(), q1, "5");
        Tentativa fechada = service.encerrar(bia.getId());
        confere("Status ENCERRADA", fechada.getStatus() == Tentativa.Status.ENCERRADA);
        confere("Mantém a resposta já registrada", "5".equals(fechada.getResposta(q1)));
        exigeErro("Bia tenta abrir de novo", "não pode abri-la novamente", () -> service.iniciar(prova.getId(), "BIA"));

        System.out.println("--- Tempo acaba ---");
        relogio.irPara(em(26, 14, 0));
        Tentativa caio = service.iniciar(prova.getId(), "caio");
        service.responder(caio.getId(), q1, "4");
        relogio.irPara(em(26, 14, 59));
        service.responder(caio.getId(), q3, "Resposta no último minuto");
        relogio.irPara(em(26, 15, 1));
        exigeErro("Responder depois do limite", "tempo da avaliação acabou", () -> service.responder(caio.getId(), q2, "Falso"));
        Tentativa depois = service.finalizar(caio.getId());
        confere("Status ENCERRADA por tempo", depois.getStatus() == Tentativa.Status.ENCERRADA);
        confere("Fim registrado no limite", depois.getFim().equals(em(26, 15, 0)));
        confere("Manteve as 2 respostas dadas antes do limite", depois.getRespostas().size() == 2);
        confere("Cronômetro zerado", service.segundosRestantes(caio.getId()) == 0);

        System.out.println("--- Prazo vence antes da duração ---");
        relogio.irPara(em(28, 17, 30));
        Tentativa davi = service.iniciar(prova.getId(), "davi");
        confere("Limite = prazo (18:00), e não 18:30", davi.getLimite().equals(em(28, 18, 0)));

        System.out.println("--- Outras regras ---");
        exigeErro("Questão de outra avaliação", "não pertence", () -> service.responder(davi.getId(), outra, "x"));
        service.responder(davi.getId(), q1, "4");
        service.responder(davi.getId(), q1, "  ");               // resposta vazia apaga
        confere("Resposta vazia apaga a anterior", service.finalizar(davi.getId()).getRespostas().isEmpty());
        relogio.irPara(em(28, 18, 0));
        exigeErro("Depois do prazo", "prazo da avaliação terminou", () -> service.iniciar(prova.getId(), "eva"));

        // Sem duração definida, só vale o prazo
        Avaliacao semDuracao = repoAval.salvar("Prova 2", List.of(q1), "1A",
                LocalDate.of(2026, 10, 28), LocalDate.of(2026, 10, 30));
        semDuracao.setHoraInicio(LocalTime.of(8, 0));
        semDuracao.setHoraFim(LocalTime.of(12, 0));
        semDuracao.setDisponibilizada(true);
        Tentativa fabio = service.iniciar(semDuracao.getId(), "fabio");
        confere("Duração 0: limite = prazo (30/10 12:00)", fabio.getLimite().equals(em(30, 12, 0)));

        System.out.println("--- Avaliação criada pelo service (como a tela Nova Prova faz) ---");
        AvaliacaoService criador = new AvaliacaoService(repoAval);
        Avaliacao daTela = criador.criar("Prova da tela", List.of(q1, q2), "1A",
                LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 3),
                LocalTime.of(8, 0), LocalTime.of(10, 0), 30);
        daTela.setDisponibilizada(true);
        relogio.irPara(LocalDateTime.of(2026, 11, 1, 12, 0));
        exigeErro("Antes do horário de início (02/11 08:00)", "ainda não começou", () -> service.iniciar(daTela.getId(), "gabi"));
        relogio.irPara(LocalDateTime.of(2026, 11, 2, 9, 0));
        Tentativa gabi = service.iniciar(daTela.getId(), "gabi");
        confere("Duração de 30 min vinda da tela: limite = 09:30", gabi.getLimite().equals(LocalDateTime.of(2026, 11, 2, 9, 30)));
        confere("Cronômetro mostra 30 min (1800 s)", service.segundosRestantes(gabi.getId()) == 1800);
        service.responder(gabi.getId(), q1, "4");
        relogio.irPara(LocalDateTime.of(2026, 11, 2, 9, 30));
        confere("Ao chegar no limite, o cronômetro zera", service.segundosRestantes(gabi.getId()) == 0);
        confere("...e a tentativa fica ENCERRADA com a resposta salva",
                gabi.getStatus() == Tentativa.Status.ENCERRADA && "4".equals(gabi.getResposta(q1)));
        confere("Avaliação criada pela tela conta como respondida depois que alguém abre", daTela.isRespondida());
        Avaliacao v2daTela = criador.editar(daTela.getId(), "Prova da tela", List.of(q1), "1A",
                LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 3), LocalTime.of(8, 0), LocalTime.of(10, 0), 30);
        confere("Editar depois de aberta cria a versão 2 e preserva a tentativa da original",
                v2daTela.getNome().equals("Prova da tela2") && service.motivoDeBloqueio(daTela.getId(), "gabi") != null
                        && repoTent.listarPorAvaliacao(daTela.getId()).size() == 1);

        System.out.println("--- Apoio da tela Responder Avaliação ---");
        confere("Tempo 0 -> 00:00:00", ApoioResposta.formatarTempo(0).equals("00:00:00"));
        confere("Tempo 59 -> 00:00:59", ApoioResposta.formatarTempo(59).equals("00:00:59"));
        confere("Tempo 3661 -> 01:01:01", ApoioResposta.formatarTempo(3661).equals("01:01:01"));
        confere("Tempo 90000 (25 h) -> 25:00:00", ApoioResposta.formatarTempo(90000).equals("25:00:00"));
        confere("Tempo negativo nunca aparece", ApoioResposta.formatarTempo(-5).equals("00:00:00"));
        List<Questao.ParColuna> pares = List.of(new Questao.ParColuna("Java", "Linguagem"), new Questao.ParColuna("MVC", "Padrão"),
                new Questao.ParColuna("SQL", "Consulta"));
        confere("Ligar colunas: mesmo formato do gabarito (A → B; C → D)",
                ApoioResposta.montarRespostaColunas(pares, java.util.Arrays.asList("Linguagem", "Padrão", "Consulta"))
                        .equals("Java → Linguagem; MVC → Padrão; SQL → Consulta"));
        confere("Ligar colunas: pares sem escolha ficam de fora",
                ApoioResposta.montarRespostaColunas(pares, java.util.Arrays.asList(null, "Padrão", " ")).equals("MVC → Padrão"));
        confere("Ligar colunas: nada escolhido -> resposta vazia (apaga a anterior)",
                ApoioResposta.montarRespostaColunas(pares, java.util.Arrays.asList(null, null, null)).isEmpty());
        Avaliacao deColunas = repoAval.salvar("Colunas", List.of(new Questao("Ligue", "Ligar Colunas", null, null, null, pares)), "1A",
                LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 6));
        deColunas.setDisponibilizada(true);
        relogio.irPara(LocalDateTime.of(2026, 11, 5, 1, 0));
        Tentativa hugo = service.iniciar(deColunas.getId(), "hugo");
        Questao ligue = deColunas.getQuestoes().get(0);
        service.responder(hugo.getId(), ligue, ApoioResposta.montarRespostaColunas(pares, java.util.Arrays.asList("Linguagem", "Padrão", null)));
        confere("Questão de ligar colunas guarda a resposta em texto", "Java → Linguagem; MVC → Padrão".equals(service.finalizar(hugo.getId()).getResposta(ligue)));

        System.out.println();
        System.out.println(ok + " verificações OK, " + falhas + " falhas.");
        if (falhas > 0) System.exit(1);
    }
}
