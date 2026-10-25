package br.snctech.correcao.controller;

import br.snctech.NavegacaoUtil;
import br.snctech.correcao.DadosCorrecao;
import br.snctech.correcao.controller.PecasVisuais.Cor;
import br.snctech.correcao.model.AvaliacaoRespondida;
import br.snctech.correcao.model.QuestaoCorrecao;
import br.snctech.correcao.model.RespostaQuestao;
import br.snctech.correcao.model.StatusCorrecao;
import br.snctech.correcao.model.TipoQuestao;
import br.snctech.correcao.model.VereditoQuestao;
import br.snctech.correcao.service.ResumoCorrecaoService.LinhaAluno;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tela 7 - Correção manual do aluno (PB-03).
 *
 * <p>Só desenha e chama os serviços; a regra de correção está em {@code CorrecaoManualService}.
 * O professor pode validar a nota sugerida ou alterar a nota de QUALQUER questão
 * (inclusive as que o sistema corrigiu sozinho); cada ação é salva na hora.
 */
public class CorrecaoManualController {

    @FXML private Label tituloTela;
    @FXML private ComboBox<LinhaAluno> seletorAluno;
    @FXML private VBox colunaResumo;
    @FXML private VBox listaQuestoes;
    @FXML private ScrollPane rolagem;
    @FXML private Button botaoVoltar;

    private Integer idProvaAtual;
    private String nomeAlunoAtual;

    @FXML
    private void initialize() {
        var ctx = DadosCorrecao.contexto();
        tituloTela.setText(ctx.nomeAvaliacao());

        seletorAluno.setConverter(new StringConverter<>() {
            @Override
            public String toString(LinhaAluno linha) {
                return linha == null ? "" : linha.nome();
            }

            @Override
            public LinhaAluno fromString(String texto) {
                return null;
            }
        });

        List<LinhaAluno> alunos = DadosCorrecao.resumo()
                .resumir(ctx.idAvaliacao(), DadosCorrecao.prazoEncerrado(ctx.idAvaliacao())).alunos().stream()
                .filter(l -> l.idAvaliacaoRespondida() != null)
                .toList();
        seletorAluno.getItems().setAll(alunos);

        seletorAluno.valueProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                mostrar(novo.idAvaliacaoRespondida(), novo.nome());
            }
        });

        if (alunos.isEmpty()) {
            listaQuestoes.getChildren().setAll(new Label("Nenhum aluno respondeu esta avaliação ainda."));
            return;
        }
        Integer desejado = DadosCorrecao.alunoSelecionado();
        LinhaAluno inicial = alunos.stream()
                .filter(l -> l.idAluno().equals(desejado))
                .findFirst().orElse(alunos.get(0));
        seletorAluno.getSelectionModel().select(inicial);
    }

    // ------------------------------------------------------------------ conteúdo

    private void mostrar(Integer idProva, String nomeAluno) {
        idProvaAtual = idProva;
        nomeAlunoAtual = nomeAluno;
        var ctx = DadosCorrecao.contexto();

        AvaliacaoRespondida ar = DadosCorrecao.respondidas().buscar(idProva);
        List<QuestaoCorrecao> questoes = DadosCorrecao.questoes().buscarPorAvaliacao(ar.getIdAvaliacao());

        tituloTela.setText(ctx.nomeAvaliacao() + " · " + nomeAluno);
        colunaResumo.getChildren().setAll(cartaoResumo(nomeAluno, ctx.nomeTurma(), ar));

        double rolagemAtual = rolagem.getVvalue();
        listaQuestoes.getChildren().clear();
        int numero = 1;
        for (QuestaoCorrecao q : questoes) {
            RespostaQuestao r = ar.resposta(q.id()).orElseGet(() -> new RespostaQuestao(q.id()));
            listaQuestoes.getChildren().add(cartaoQuestao(numero++, q, r, ar));
        }
        rolagem.setVvalue(rolagemAtual);
    }

    private VBox cartaoResumo(String nomeAluno, String nomeTurma, AvaliacaoRespondida ar) {
        boolean fechada = ar.notaFinalConfirmada();

        Label nota = new Label(PecasVisuais.formatar(ar.notaTotal()));
        nota.getStyleClass().add("corr-nota-grande");

        Label aviso = new Label("Cada \"Validar nota\" ou \"Alterar nota\" é salvo na hora.");
        aviso.setWrapText(true);
        aviso.getStyleClass().add("corr-aviso");

        VBox cartao = PecasVisuais.cartao(8);
        cartao.getChildren().addAll(
                new Label("Aluno: " + nomeAluno),
                new Label("Turma: " + nomeTurma),
                new Label(fechada ? "Nota final:" : "Nota parcial:"),
                nota,
                PecasVisuais.selo(fechada ? "Corrigida" : "Parcial", fechada ? Cor.VERDE : Cor.LARANJA),
                aviso);
        return cartao;
    }

    private VBox cartaoQuestao(int numero, QuestaoCorrecao q, RespostaQuestao r, AvaliacaoRespondida ar) {
        Label titulo = new Label("Questão " + numero + " · " + rotuloTipo(q.tipo()));
        titulo.getStyleClass().add("corr-titulo-questao");

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox topo = new HBox(10, titulo, espaco, seloDaQuestao(q, r));
        topo.setAlignment(Pos.CENTER_LEFT);

        Label enunciado = new Label(DadosCorrecao.enunciados().enunciado(q.id()));
        enunciado.setWrapText(true);

        VBox cartao = PecasVisuais.cartao(8);
        cartao.getChildren().addAll(topo, enunciado);

        if (q.tipo() == TipoQuestao.DISCURSIVA) {
            cartao.getChildren().add(PecasVisuais.secao("RESPOSTA DO ALUNO",
                    r.getTexto() == null || r.getTexto().isBlank() ? "(em branco)" : r.getTexto()));
            cartao.getChildren().add(PecasVisuais.secao("RESPOSTA ESPERADA",
                    q.respostaEsperada() == null ? "(não cadastrada)" : q.respostaEsperada()));
            cartao.getChildren().add(PecasVisuais.secao("PRÉ-CORREÇÃO DO SISTEMA",
                    r.getNotaSugerida() == null
                            ? "Sem nota sugerida."
                            : "Nota sugerida: " + PecasVisuais.formatar(r.getNotaSugerida())
                                + " de " + PecasVisuais.formatar(q.valor())));
        } else {
            cartao.getChildren().add(PecasVisuais.secao("RESPOSTA DO ALUNO", descrever(q, r, false)));
            cartao.getChildren().add(PecasVisuais.secao("GABARITO", descrever(q, r, true)));
        }
        cartao.getChildren().add(controles(q, r, ar));
        return cartao;
    }

    private HBox controles(QuestaoCorrecao q, RespostaQuestao r, AvaliacaoRespondida ar) {
        int idProfessor = DadosCorrecao.idProfessor();

        TextField campo = new TextField();
        campo.setPrefWidth(80);
        BigDecimal inicial = r.getNota() != null ? r.getNota() : r.getNotaSugerida();
        if (inicial != null) {
            campo.setText(PecasVisuais.formatar(inicial));
        }

        Button validar = new Button("Validar nota");
        validar.getStyleClass().add("secondary-button");
        validar.setDisable(r.getNotaSugerida() == null || r.getStatus() == StatusCorrecao.CORRIGIDA_MANUALMENTE);
        validar.setOnAction(e ->
                executar(() -> DadosCorrecao.correcaoManual().validarSugestao(ar.getId(), q.id(), idProfessor)));

        Button alterar = new Button("Alterar nota");
        alterar.getStyleClass().add("primary-button");
        alterar.setOnAction(e -> {
            BigDecimal nota = PecasVisuais.lerNota(campo.getText());
            if (nota == null) {
                PecasVisuais.alerta("Digite um número válido, por exemplo 1,5.");
                return;
            }
            executar(() -> DadosCorrecao.correcaoManual().lancarNota(ar.getId(), q.id(), nota, idProfessor));
        });

        HBox linha = new HBox(8, new Label("Nota (máx. " + PecasVisuais.formatar(q.valor()) + ")"), campo, validar, alterar);
        linha.setAlignment(Pos.CENTER_LEFT);
        return linha;
    }

    /** Roda a ação do serviço; se a regra recusar (nota inválida etc.), mostra o motivo. Depois redesenha. */
    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            PecasVisuais.alerta(ex.getMessage());
            return;
        }
        mostrar(idProvaAtual, nomeAlunoAtual);
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        NavegacaoUtil.abrir((Node) evento.getSource(), "correcao-alunos.fxml", "SNCTECH - Correção da prova");
    }

    // ------------------------------------------------------------------ textos

    private Label seloDaQuestao(QuestaoCorrecao q, RespostaQuestao r) {
        if (r.getStatus() == StatusCorrecao.PENDENTE_MANUAL || r.getStatus() == null) {
            return PecasVisuais.selo("Aguardando professor", Cor.LARANJA);
        }
        String detalhe = " (" + PecasVisuais.formatar(r.getNota()) + " de " + PecasVisuais.formatar(q.valor()) + ")";
        if (r.getStatus() == StatusCorrecao.CORRIGIDA_MANUALMENTE) {
            return PecasVisuais.selo("Corrigida pelo professor" + detalhe, Cor.VERDE);
        }
        return switch (VereditoQuestao.de(r, q.valor())) {
            case CORRETA -> PecasVisuais.selo("Correta · automática" + detalhe, Cor.VERDE);
            case PARCIAL -> PecasVisuais.selo("Parcial · automática" + detalhe, Cor.AZUL);
            case INCORRETA -> PecasVisuais.selo("Incorreta · automática" + detalhe, Cor.VERMELHO);
            case AGUARDANDO_PROFESSOR -> PecasVisuais.selo("Aguardando professor", Cor.LARANJA);
        };
    }

    /** Texto da resposta do aluno (gabarito=false) ou do gabarito (gabarito=true) de uma questão objetiva. */
    private static String descrever(QuestaoCorrecao q, RespostaQuestao r, boolean gabarito) {
        return switch (q.tipo()) {
            case MULTIPLA_ESCOLHA -> alternativa(gabarito ? q.gabaritoAlternativaId() : r.getAlternativaId());
            case VERDADEIRO_FALSO -> {
                String id = gabarito ? q.gabaritoAlternativaId() : r.getAlternativaId();
                yield "V".equals(id) ? "Verdadeiro" : "F".equals(id) ? "Falso" : "(em branco)";
            }
            case ASSOCIACAO -> pares(gabarito ? q.gabaritoPares() : r.getPares());
            case DISCURSIVA -> gabarito ? String.valueOf(q.respostaEsperada()) : String.valueOf(r.getTexto());
        };
    }

    private static String alternativa(String id) {
        return id == null || id.isBlank() ? "(em branco)" : "Alternativa " + id;
    }

    private static String pares(Map<String, String> pares) {
        if (pares == null || pares.isEmpty()) {
            return "(em branco)";
        }
        return pares.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + " → " + e.getValue())
                .collect(Collectors.joining("; "));
    }

    private static String rotuloTipo(TipoQuestao tipo) {
        return switch (tipo) {
            case MULTIPLA_ESCOLHA -> "Múltipla escolha";
            case VERDADEIRO_FALSO -> "Verdadeiro/Falso";
            case ASSOCIACAO -> "Associação";
            case DISCURSIVA -> "Discursiva";
        };
    }
}
