package br.snctech;

import br.snctech.Repositorio.RepositorioAvaliacoes;
import br.snctech.Repositorio.RepositorioAvaliacoesMemoria;
import br.snctech.model.Avaliacao;
import br.snctech.service.AvaliacaoService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Teste de console das regras de criação e edição de avaliações (PB-01), sem telas.
 * Rodar o main. Cada linha mostra OK ou FALHOU; no fim há o resumo.
 */
public class TesteAvaliacao {

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

    static final LocalDate DATA = LocalDate.of(2026, 10, 26);
    static final LocalDate PRAZO = LocalDate.of(2026, 10, 28);

    public static void main(String[] args) {
        Questao q1 = new Questao("Quanto é 2+2?", "Múltipla Escolha", null, List.of("3", "4", "5"), "4", null);
        Questao q2 = new Questao("A Terra é plana?", "Verdadeiro/Falso", null, null, "Falso", null);
        Questao q3 = new Questao("Explique MVC.", "Dissertativa", null, null, null, null);
        List<Questao> questoes = List.of(q1, q2);

        System.out.println("--- Criar avaliação: caminho feliz ---");
        RepositorioAvaliacoes repo = new RepositorioAvaliacoesMemoria();
        AvaliacaoService service = new AvaliacaoService(repo);
        Avaliacao prova = service.criar("  Prova 1  ", questoes, "  1A  ", DATA, PRAZO);
        confere("Nome e turma são salvos sem espaços sobrando", prova.getNome().equals("Prova 1") && prova.getTurma().equals("1A"));
        confere("Guarda data, prazo e as questões escolhidas",
                prova.getData().equals(DATA) && prova.getPrazo().equals(PRAZO) && prova.getQuestoes().size() == 2);
        confere("Começa não respondida e não disponibilizada", !prova.isRespondida() && !prova.isDisponibilizada());
        confere("Aparece na lista e é encontrada pelo id",
                repo.listarTodas().size() == 1 && repo.buscarPorId(prova.getId()) == prova);
        confere("Prazo no mesmo dia da data é aceito", service.criar("Prova do dia", questoes, "1A", DATA, DATA) != null);
        confere("Cada avaliação recebe um id diferente", repo.listarTodas().get(0).getId() != repo.listarTodas().get(1).getId());

        System.out.println("--- Criar avaliação: validações ---");
        int antes = repo.listarTodas().size();
        exigeErro("Sem nome", "nome", () -> service.criar("   ", questoes, "1A", DATA, PRAZO));
        exigeErro("Nome nulo", "nome", () -> service.criar(null, questoes, "1A", DATA, PRAZO));
        exigeErro("Sem questão", "pelo menos uma questão", () -> service.criar("P", List.of(), "1A", DATA, PRAZO));
        exigeErro("Lista de questões nula", "pelo menos uma questão", () -> service.criar("P", null, "1A", DATA, PRAZO));
        exigeErro("Sem turma", "turma", () -> service.criar("P", questoes, " ", DATA, PRAZO));
        exigeErro("Sem data", "data", () -> service.criar("P", questoes, "1A", null, PRAZO));
        exigeErro("Sem prazo", "prazo", () -> service.criar("P", questoes, "1A", DATA, null));
        exigeErro("Prazo antes da data", "anterior", () -> service.criar("P", questoes, "1A", PRAZO, DATA));
        confere("Nenhuma avaliação inválida foi guardada", repo.listarTodas().size() == antes);

        System.out.println("--- Editar avaliação ainda não respondida ---");
        Avaliacao editada = service.editar(prova.getId(), "Prova 1 revisada", List.of(q1, q2, q3), "1B", DATA, PRAZO.plusDays(1));
        confere("Altera a própria avaliação (mesmo id, mesma instância)", editada == prova && editada.getId() == prova.getId());
        confere("Novo nome, turma, prazo e questões",
                prova.getNome().equals("Prova 1 revisada") && prova.getTurma().equals("1B")
                        && prova.getPrazo().equals(PRAZO.plusDays(1)) && prova.getQuestoes().size() == 3);
        confere("Não cria avaliação nova", repo.listarTodas().size() == antes);
        exigeErro("Editar para nome vazio", "nome", () -> service.editar(prova.getId(), " ", questoes, "1B", DATA, PRAZO));
        exigeErro("Editar sem questões", "pelo menos uma questão", () -> service.editar(prova.getId(), "P", List.of(), "1B", DATA, PRAZO));
        exigeErro("Editar com prazo antes da data", "anterior", () -> service.editar(prova.getId(), "P", questoes, "1B", PRAZO, DATA));
        exigeErro("Editar avaliação que não existe", "não foi encontrada", () -> service.editar(999, "P", questoes, "1B", DATA, PRAZO));
        confere("Edição recusada não mexe na avaliação", prova.getNome().equals("Prova 1 revisada"));
        Avaliacao so3 = service.editar(prova.getId(), "Só nome e questões", List.of(q1));
        confere("Editar só nome e questões mantém turma, data e prazo",
                so3.getTurma().equals("1B") && so3.getData().equals(DATA) && so3.getPrazo().equals(PRAZO.plusDays(1)));

        System.out.println("--- Editar avaliação já respondida (cria nova versão) ---");
        RepositorioAvaliacoes repo2 = new RepositorioAvaliacoesMemoria();
        AvaliacaoService service2 = new AvaliacaoService(repo2);
        Avaliacao original = service2.criar("Avaliação", questoes, "1A", DATA, PRAZO);
        original.setRespondida(true);
        Avaliacao v2 = service2.editar(original.getId(), "Avaliação", List.of(q1, q2, q3), "1A", DATA, PRAZO);
        confere("Mantendo o nome, a nova versão ganha o número 2 no final", v2.getNome().equals("Avaliação2"));
        confere("É outra avaliação, e a original continua igual",
                v2 != original && v2.getId() != original.getId()
                        && original.getNome().equals("Avaliação") && original.getQuestoes().size() == 2);
        confere("A nova versão não nasce respondida", !v2.isRespondida());
        confere("As duas ficam na lista", repo2.listarTodas().size() == 2);
        Avaliacao v3 = service2.editar(original.getId(), "avaliação", questoes, "1A", DATA, PRAZO);
        confere("Já existe a 2: a próxima versão vira 3 (ignora maiúsculas)", v3.getNome().equals("avaliação3"));
        Avaliacao outroNome = service2.editar(original.getId(), "Avaliação final", questoes, "1A", DATA, PRAZO);
        confere("Trocando o nome, a nova versão usa o nome novo sem número", outroNome.getNome().equals("Avaliação final"));
        confere("Original nunca foi alterada", original.getNome().equals("Avaliação") && repo2.listarTodas().size() == 4);

        System.out.println("--- Horário e duração (campos novos da tela) ---");
        RepositorioAvaliacoes repo3 = new RepositorioAvaliacoesMemoria();
        AvaliacaoService service3 = new AvaliacaoService(repo3);
        LocalTime oito = LocalTime.of(8, 0), dezoito = LocalTime.of(18, 0);
        Avaliacao comHorario = service3.criar("Prova 2", questoes, "1A", DATA, PRAZO, oito, dezoito, 60);
        confere("Guarda hora de início, de término e duração",
                comHorario.getHoraInicio().equals(oito) && comHorario.getHoraFim().equals(dezoito) && comHorario.getDuracaoMinutos() == 60);
        confere("Duração 0 (sem limite além do prazo) é aceita", service3.criar("Prova 3", questoes, "1A", DATA, PRAZO, oito, dezoito, 0).getDuracaoMinutos() == 0);
        confere("No mesmo dia, término depois do início é aceito", service3.criar("Prova 4", questoes, "1A", DATA, DATA, oito, dezoito, 30) != null);
        exigeErro("Sem hora de início", "hora de início", () -> service3.criar("P", questoes, "1A", DATA, PRAZO, null, dezoito, 60));
        exigeErro("Sem hora de término", "hora de término", () -> service3.criar("P", questoes, "1A", DATA, PRAZO, oito, null, 60));
        exigeErro("Mesmo dia com término antes do início", "depois do início", () -> service3.criar("P", questoes, "1A", DATA, DATA, dezoito, oito, 60));
        exigeErro("Mesmo dia com término igual ao início", "depois do início", () -> service3.criar("P", questoes, "1A", DATA, DATA, oito, oito, 60));
        confere("Dias diferentes: o horário de término pode ser menor que o de início",
                service3.criar("Prova 5", questoes, "1A", DATA, PRAZO, dezoito, oito, 60) != null);
        exigeErro("Duração negativa", "duração", () -> service3.criar("P", questoes, "1A", DATA, PRAZO, oito, dezoito, -5));
        exigeErro("Os horários também são validados ao editar", "depois do início",
                () -> service3.editar(comHorario.getId(), "Prova 2", questoes, "1A", DATA, DATA, dezoito, oito, 60));

        Avaliacao alterada = service3.editar(comHorario.getId(), "Prova 2", questoes, "1A", DATA, PRAZO, LocalTime.of(9, 30), LocalTime.of(12, 0), 45);
        confere("Editar altera horário e duração na própria avaliação",
                alterada == comHorario && comHorario.getHoraInicio().equals(LocalTime.of(9, 30))
                        && comHorario.getHoraFim().equals(LocalTime.of(12, 0)) && comHorario.getDuracaoMinutos() == 45);
        Avaliacao semNovos = service3.editar(comHorario.getId(), "Prova 2 b", questoes, "1B", DATA, PRAZO);
        confere("Editar sem informar horário mantém o que já estava",
                semNovos.getHoraInicio().equals(LocalTime.of(9, 30)) && semNovos.getDuracaoMinutos() == 45);
        comHorario.setRespondida(true);
        Avaliacao versaoNova = service3.editar(comHorario.getId(), "Prova 2 b", questoes, "1B", DATA, PRAZO);
        confere("Nova versão (avaliação respondida) herda horário e duração em vez de perdê-los",
                versaoNova != comHorario && versaoNova.getHoraFim().equals(LocalTime.of(12, 0)) && versaoNova.getDuracaoMinutos() == 45);
        Avaliacao versaoComNovos = service3.editar(comHorario.getId(), "Prova 2 c", questoes, "1B", DATA, PRAZO, oito, dezoito, 90);
        confere("Nova versão com horário e duração informados usa os novos valores",
                versaoComNovos.getHoraInicio().equals(oito) && versaoComNovos.getDuracaoMinutos() == 90
                        && comHorario.getDuracaoMinutos() == 45);

        System.out.println();
        System.out.println(ok + " verificações OK, " + falhas + " falhas.");
        if (falhas > 0) System.exit(1);
    }
}
