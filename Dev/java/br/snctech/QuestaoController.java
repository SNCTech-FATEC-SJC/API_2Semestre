package br.snctech;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.snctech.Questao.ParColuna;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class QuestaoController {

    @FXML private TextArea campoEnunciado;
    @FXML private ComboBox<String> cbTipo;

    @FXML private VBox painelMultiplaEscolha;
    @FXML private VBox painelVerdadeiroFalso;
    @FXML private VBox painelLigarColunas;
    @FXML private VBox painelDissertativa;

    @FXML private TextField campoAlternativaA;
    @FXML private TextField campoAlternativaB;
    @FXML private TextField campoAlternativaC;
    @FXML private TextField campoAlternativaD;

    @FXML private RadioButton radioA;
    @FXML private RadioButton radioB;
    @FXML private RadioButton radioC;
    @FXML private RadioButton radioD;

    @FXML private RadioButton radioVerdadeiro;
    @FXML private RadioButton radioFalso;

    @FXML private TextArea campoRespostaDissertativa;

    @FXML private TextField campoTermoA;
    @FXML private TextField campoTermoB;

    @FXML private ListView<ParColuna> listaPares;
    @FXML private Label lblImagem;
    @FXML private Label mensagem;
    @FXML private ImageView previewImagem;
    @FXML private Button botaoVoltar;
    @FXML private Button botaoSalvar;

    private ToggleGroup grupoAlternativas;
    private ToggleGroup grupoVerdadeiroFalso;

    private final List<ParColuna> paresForm = new ArrayList<>();

    private File imagemSelecionada;
    private String imagemPathExistente;
    private Questao questaoEmEdicao;

    @FXML
    private void initialize() {
        cbTipo.getItems().setAll(
                "Múltipla Escolha",
                "Dissertativa",
                "Verdadeiro/Falso",
                "Ligar Colunas"
        );

        grupoAlternativas = new ToggleGroup();

        radioA.setToggleGroup(grupoAlternativas);
        radioB.setToggleGroup(grupoAlternativas);
        radioC.setToggleGroup(grupoAlternativas);
        radioD.setToggleGroup(grupoAlternativas);

        grupoVerdadeiroFalso = new ToggleGroup();

        radioVerdadeiro.setToggleGroup(grupoVerdadeiroFalso);
        radioFalso.setToggleGroup(grupoVerdadeiroFalso);

        listaPares.getItems().setAll(paresForm);

        cbTipo.setOnAction(event -> alternarCamposPorTipo());

        questaoEmEdicao = BancoQuestoes.getQuestaoEmEdicao();

        if (questaoEmEdicao != null
                && BancoQuestoes.contem(questaoEmEdicao)) {
            carregarQuestaoParaEdicao(questaoEmEdicao);
        } else {
            BancoQuestoes.limparEdicao();
            cbTipo.setValue("Múltipla Escolha");
            radioA.setSelected(true);
            radioVerdadeiro.setSelected(true);
            alternarCamposPorTipo();
        }
    }

    private void alternarCamposPorTipo() {
        String tipo = cbTipo.getValue();

        exibirPainel(
                painelMultiplaEscolha,
                "Múltipla Escolha".equals(tipo)
        );

        exibirPainel(
                painelDissertativa,
                "Dissertativa".equals(tipo)
        );

        exibirPainel(
                painelVerdadeiroFalso,
                "Verdadeiro/Falso".equals(tipo)
        );

        exibirPainel(
                painelLigarColunas,
                "Ligar Colunas".equals(tipo)
        );
    }

    private void exibirPainel(VBox painel, boolean exibir) {
        painel.setVisible(exibir);
        painel.setManaged(exibir);
    }

    private void carregarQuestaoParaEdicao(Questao questao) {
        campoEnunciado.setText(questao.getEnunciado());
        cbTipo.setValue(questao.getTipo());

        List<String> alternativas = questao.getAlternativas();

        if (alternativas.size() > 0) {
            campoAlternativaA.setText(alternativas.get(0));
        }

        if (alternativas.size() > 1) {
            campoAlternativaB.setText(alternativas.get(1));
        }

        if (alternativas.size() > 2) {
            campoAlternativaC.setText(alternativas.get(2));
        }

        if (alternativas.size() > 3) {
            campoAlternativaD.setText(alternativas.get(3));
        }

        String resposta = questao.getRespostaCorreta();

        if ("A".equals(resposta)) {
            radioA.setSelected(true);
        } else if ("B".equals(resposta)) {
            radioB.setSelected(true);
        } else if ("C".equals(resposta)) {
            radioC.setSelected(true);
        } else if ("D".equals(resposta)) {
            radioD.setSelected(true);
        }

        if ("Verdadeiro".equals(resposta)) {
            radioVerdadeiro.setSelected(true);
        } else if ("Falso".equals(resposta)) {
            radioFalso.setSelected(true);
        }

        if ("Dissertativa".equals(questao.getTipo())) {
            campoRespostaDissertativa.setText(resposta);
        }

        paresForm.clear();
        paresForm.addAll(questao.getPares());
        listaPares.getItems().setAll(paresForm);

        imagemPathExistente = questao.getImagemPath();

        if (imagemPathExistente != null
                && !imagemPathExistente.isBlank()) {
            File arquivo = new File(imagemPathExistente);

            if (arquivo.isFile()) {
                try {
                    Image imagem = new Image(arquivo.toURI().toString());

                    if (!imagem.isError()) {
                        previewImagem.setImage(imagem);
                        lblImagem.setText(arquivo.getName());
                    }
                } catch (RuntimeException erro) {
                    lblImagem.setText(
                            "Imagem cadastrada, mas não foi possível carregá-la."
                    );
                }
            } else {
                lblImagem.setText(
                        "Arquivo de imagem não encontrado."
                );
            }
        }

        botaoSalvar.setText("Salvar alterações");
        mensagem.setText("Editando questão existente.");

        alternarCamposPorTipo();
    }

    @FXML
    private void aoClicarAdicionarPar() {
        String termoA = campoTermoA.getText().trim();
        String termoB = campoTermoB.getText().trim();

        if (termoA.isEmpty() || termoB.isEmpty()) {
            exibirErro(
                    "Preencha os dois termos antes de adicionar o par."
            );
            return;
        }

        paresForm.add(new ParColuna(termoA, termoB));
        listaPares.getItems().setAll(paresForm);

        campoTermoA.clear();
        campoTermoB.clear();
        mensagem.setText("Par adicionado.");
    }

    @FXML
    private void aoClicarRemoverPar() {
        ParColuna selecionado =
                listaPares.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            exibirErro("Selecione um par para remover.");
            return;
        }

        paresForm.remove(selecionado);
        listaPares.getItems().setAll(paresForm);
    }

    @FXML
    private void aoClicarSelecionarImagem() {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Selecionar imagem da questão");

        seletor.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Imagens",
                        "*.png", "*.jpg", "*.jpeg", "*.gif"
                )
        );

        Stage palco =
                (Stage) campoEnunciado.getScene().getWindow();

        File arquivo = seletor.showOpenDialog(palco);

        if (arquivo == null) {
            return;
        }

        try {
            Image imagem = new Image(arquivo.toURI().toString());

            if (imagem.isError()) {
                exibirErro("Não foi possível carregar essa imagem.");
                return;
            }

            imagemSelecionada = arquivo;
            imagemPathExistente = null;

            previewImagem.setImage(imagem);
            lblImagem.setText(arquivo.getName());
            mensagem.setText("Imagem selecionada.");
        } catch (RuntimeException erro) {
            exibirErro("Não foi possível carregar essa imagem.");
        }
    }

    @FXML
    private void aoClicarRemoverImagem() {
        imagemSelecionada = null;
        imagemPathExistente = null;

        previewImagem.setImage(null);
        lblImagem.setText("Nenhuma imagem selecionada");
    }

    @FXML
    private void aoClicarSalvar() {
        String enunciado = campoEnunciado.getText().trim();
        String tipo = cbTipo.getValue();

        if (enunciado.isEmpty()) {
            exibirErro("O enunciado da questão é obrigatório.");
            campoEnunciado.requestFocus();
            return;
        }

        if (tipo == null || tipo.isBlank()) {
            exibirErro("Selecione um tipo de questão válido.");
            return;
        }

        List<String> alternativas = new ArrayList<>();
        List<ParColuna> pares = new ArrayList<>();
        String respostaCorreta;

        switch (tipo) {
            case "Múltipla Escolha":
                alternativas = Arrays.asList(
                        campoAlternativaA.getText().trim(),
                        campoAlternativaB.getText().trim(),
                        campoAlternativaC.getText().trim(),
                        campoAlternativaD.getText().trim()
                );

                for (int i = 0; i < alternativas.size(); i++) {
                    if (alternativas.get(i).isEmpty()) {
                        exibirErro(
                                "Preencha a alternativa "
                                + (char) ('A' + i) + "."
                        );
                        return;
                    }
                }

                if (grupoAlternativas.getSelectedToggle() == null) {
                    exibirErro("Selecione a alternativa correta.");
                    return;
                }

                if (grupoAlternativas.getSelectedToggle() == radioA) {
                    respostaCorreta = "A";
                } else if (
                        grupoAlternativas.getSelectedToggle() == radioB) {
                    respostaCorreta = "B";
                } else if (
                        grupoAlternativas.getSelectedToggle() == radioC) {
                    respostaCorreta = "C";
                } else {
                    respostaCorreta = "D";
                }
                break;

            case "Verdadeiro/Falso":
                if (grupoVerdadeiroFalso.getSelectedToggle() == null) {
                    exibirErro("Selecione Verdadeiro ou Falso.");
                    return;
                }

                respostaCorreta =
                        grupoVerdadeiroFalso.getSelectedToggle()
                                == radioVerdadeiro
                        ? "Verdadeiro"
                        : "Falso";
                break;

            case "Dissertativa":
                respostaCorreta =
                        campoRespostaDissertativa.getText().trim();

                if (respostaCorreta.isEmpty()) {
                    exibirErro(
                            "A resposta esperada da questão dissertativa "
                            + "é obrigatória."
                    );
                    campoRespostaDissertativa.requestFocus();
                    return;
                }
                break;

            case "Ligar Colunas":
                if (paresForm.isEmpty()) {
                    exibirErro(
                            "Adicione pelo menos um par correspondente."
                    );
                    return;
                }

                pares = new ArrayList<>(paresForm);

                StringBuilder gabarito = new StringBuilder();

                for (ParColuna par : pares) {
                    if (gabarito.length() > 0) {
                        gabarito.append("; ");
                    }

                    gabarito.append(par.getTermoA())
                            .append(" → ")
                            .append(par.getTermoB());
                }

                respostaCorreta = gabarito.toString();
                break;

            default:
                exibirErro("Selecione um tipo de questão válido.");
                return;
        }

        String caminhoImagem = imagemSelecionada != null
                ? imagemSelecionada.getAbsolutePath()
                : imagemPathExistente;

        boolean editando = questaoEmEdicao != null
                && BancoQuestoes.contem(questaoEmEdicao);

        if (editando) {
            questaoEmEdicao.setEnunciado(enunciado);
            questaoEmEdicao.setTipo(tipo);
            questaoEmEdicao.setImagemPath(caminhoImagem);
            questaoEmEdicao.setAlternativas(alternativas);
            questaoEmEdicao.setRespostaCorreta(respostaCorreta);
            questaoEmEdicao.setPares(pares);
        } else {
            Questao novaQuestao = new Questao(
                    enunciado,
                    tipo,
                    caminhoImagem,
                    alternativas,
                    respostaCorreta,
                    pares
            );

            BancoQuestoes.adicionar(novaQuestao);
        }

        limparFormulario();
        questaoEmEdicao = null;
        BancoQuestoes.limparEdicao();

        String confirmacao = editando
                ? "Questão atualizada com sucesso!"
                : "Questão cadastrada com sucesso!";

        mensagem.setText(confirmacao);

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Banco de questões");
        alerta.setHeaderText(null);
        alerta.setContentText(confirmacao);
        alerta.showAndWait();
    }

    @FXML
    private void aoClicarLimpar() {
        limparFormulario();
        mensagem.setText("Preencha os dados da questão.");
    }

    @FXML
    private void aoClicarVoltar(ActionEvent evento) {
        BancoQuestoes.limparEdicao();

        NavegacaoUtil.abrir(
                botaoVoltar,
                "banco-questoes.fxml",
                "SNCTECH - Banco de Questões"
        );
    }

    private void limparFormulario() {
        campoEnunciado.clear();

        campoAlternativaA.clear();
        campoAlternativaB.clear();
        campoAlternativaC.clear();
        campoAlternativaD.clear();

        campoRespostaDissertativa.clear();

        campoTermoA.clear();
        campoTermoB.clear();

        grupoAlternativas.selectToggle(radioA);
        grupoVerdadeiroFalso.selectToggle(radioVerdadeiro);

        paresForm.clear();
        listaPares.getItems().clear();

        imagemSelecionada = null;
        imagemPathExistente = null;

        previewImagem.setImage(null);
        lblImagem.setText("Nenhuma imagem selecionada");

        cbTipo.setValue("Múltipla Escolha");
        alternarCamposPorTipo();

        botaoSalvar.setText("Salvar questão");
        campoEnunciado.requestFocus();
    }

    private void exibirErro(String texto) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Cadastro de questão");
        alerta.setHeaderText("Verifique os dados informados");
        alerta.setContentText(texto);
        alerta.showAndWait();
    }
}