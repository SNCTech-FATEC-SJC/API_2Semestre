import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GeradordeQuestoes extends Application {

    public static class ParColuna {
        private String termoA, termoB;
        public ParColuna(String a, String b) { this.termoA = a; this.termoB = b; }
        public String getTermoA() { return termoA; }
        public String getTermoB() { return termoB; }
        @Override public String toString() { return termoA + " ➡️ " + termoB + " (Par Correto)"; }
    }

    public static class Questao {
        private String enunciado, tipo, imagemPath, respostaCorreta;
        private List<String> alternativas;
        private List<ParColuna> pares;

        public Questao(String enun, String t, String path, List<String> alts, String resp, List<ParColuna> p) {
            this.enunciado = enun; this.tipo = t; this.imagemPath = path;
            this.alternativas = alts != null ? alts : new ArrayList<>();
            this.respostaCorreta = resp; this.pares = p != null ? p : new ArrayList<>();
        }
        public String getEnunciado() { return enunciado; } public void setEnunciado(String e) { this.enunciado = e; }
        public String getTipo() { return tipo; } public void setTipo(String t) { this.tipo = t; }
        public String getImagemPath() { return imagemPath; } public void setImagemPath(String p) { this.imagemPath = p; }
        public List<String> getAlternativas() { return alternativas; } public void setAlternativas(List<String> a) { this.alternativas = a; }
        public String getRespostaCorreta() { return respostaCorreta; } public void setRespostaCorreta(String r) { this.respostaCorreta = r; }
        public List<ParColuna> getPares() { return pares; } public void setPares(List<ParColuna> p) { this.pares = p; }
    }

    private TableView<Questao> tabela = new TableView<>();
    private ObservableList<Questao> listaQuestoes = FXCollections.observableArrayList();
    private Questao questaoEmEdicao = null;
    private TextArea txtEnunciado = new TextArea();
    private ComboBox<String> cbTipo = new ComboBox<>();
    private Label lblImagemPath = new Label("Nenhuma imagem selecionada");
    private ImageView imageViewPreview = new ImageView();
    private File imagemSelecionada = null;

    private VBox painelMultiplaEscolha = new VBox(5), painelVerdadeiroFalso = new VBox(5), painelLigarColunas = new VBox(5);
    private TextField txtAlternativa = new TextField(), txtTermoA = new TextField(), txtTermoB = new TextField();
    private ListView<String> listViewAlternativas = new ListView<>();
    private ObservableList<String> listaAlternativasForm = FXCollections.observableArrayList();
    private Label lblGabaritoMultipla = new Label("Gabarito: Não definido");
    private String gabaritoMultiplaSelecionado = "";
    private ToggleGroup groupVF = new ToggleGroup();
    private RadioButton rbVerdadeiro = new RadioButton("Verdadeiro"), rbFalso = new RadioButton("Falso");
    private ListView<ParColuna> listViewPares = new ListView<>();
    private ObservableList<ParColuna> listaParesForm = FXCollections.observableArrayList();

    @Override
     public void start(Stage primaryStage) {
        primaryStage.setTitle("Painel do Professor - Banco de Questões");
        VBox painelEsquerdo = new VBox(10); painelEsquerdo.setPadding(new Insets(10)); painelEsquerdo.setPrefWidth(380);
        Label lblTituloLista = new Label("Questões Cadastradas"); lblTituloLista.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TableColumn<Questao, String> colEnunciado = new TableColumn<>("Enunciado"); colEnunciado.setCellValueFactory(new PropertyValueFactory<>("enunciado")); colEnunciado.setPrefWidth(230);
        TableColumn<Questao, String> colTipo = new TableColumn<>("Tipo"); colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo")); colTipo.setPrefWidth(120);
        tabela.getColumns().addAll(colEnunciado, colTipo); tabela.setItems(listaQuestoes);
        Button btnEditar = new Button(" Editar Selecionada"); btnEditar.setMaxWidth(Double.MAX_VALUE); btnEditar.setOnAction(e -> carregarQuestaoParaEdicao());
        painelEsquerdo.getChildren().addAll(lblTituloLista, tabela, btnEditar);

        VBox painelDireito = new VBox(10); painelDireito.setPadding(new Insets(10)); painelDireito.setPrefWidth(480); painelDireito.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ccc; -fx-border-width: 0 0 0 1px;");
        Label lblTituloForm = new Label("Criar / Editar Questão"); lblTituloForm.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        txtEnunciado.setPromptText("Digite as instruções ou enunciado da questão aqui..."); txtEnunciado.setPrefHeight(60); txtEnunciado.setWrapText(true);
        cbTipo.getItems().addAll("Múltipla Escolha", "Dissertativa", "Verdadeiro/Falso", "Ligar Colunas"); cbTipo.setValue("Múltipla Escolha"); cbTipo.setOnAction(e -> alternarCamposPorTipo());

        Button btnAddAlt = new Button(" Adicionar"); btnAddAlt.setOnAction(e -> { String t = txtAlternativa.getText().trim(); if (!t.isEmpty()) { listaAlternativasForm.add(t); txtAlternativa.clear(); } });
        listViewAlternativas.setItems(listaAlternativasForm); listViewAlternativas.setPrefHeight(90);
        Button btnDefinirGabarito = new Button(" Definir Selecionada como Correta"); btnDefinirGabarito.setOnAction(e -> { String s = listViewAlternativas.getSelectionModel().getSelectedItem(); if (s != null) { gabaritoMultiplaSelecionado = s; lblGabaritoMultipla.setText("Gabarito: " + s); } });
        painelMultiplaEscolha.getChildren().addAll(new Label("Alternativas:"), new HBox(5, txtAlternativa, btnAddAlt), listViewAlternativas, btnDefinirGabarito, lblGabaritoMultipla);

        rbVerdadeiro.setToggleGroup(groupVF); rbFalso.setToggleGroup(groupVF); rbVerdadeiro.setSelected(true);
        painelVerdadeiroFalso.getChildren().addAll(new Label("Selecione a Questão Correta:"), new HBox(15, rbVerdadeiro, rbFalso));

        Button btnAddPar = new Button(" Adicionar Par"); btnAddPar.setOnAction(e -> { String a = txtTermoA.getText().trim(); String b = txtTermoB.getText().trim(); if (!a.isEmpty() && !b.isEmpty()) { listaParesForm.add(new ParColuna(a, b)); txtTermoA.clear(); txtTermoB.clear(); } });
        listViewPares.setItems(listaParesForm); listViewPares.setPrefHeight(90);
        painelLigarColunas.getChildren().addAll(new Label("Configurar Pares Correspondentes (Gabarito Automático):"), new HBox(5, txtTermoA, txtTermoB, btnAddPar), listViewPares);

        Button btnUpload = new Button("📁 Selecionar Imagem (Opcional)"); imageViewPreview.setFitHeight(60); imageViewPreview.setPreserveRatio(true);
        btnUpload.setOnAction(e -> { FileChooser fc = new FileChooser(); fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg")); File f = fc.showOpenDialog(primaryStage); if (f != null) { imagemSelecionada = f; lblImagemPath.setText(f.getName()); imageViewPreview.setImage(new Image(f.toURI().toString())); } });

        Button btnSalvar = new Button("💾 Salvar Questão"); btnSalvar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;"); btnSalvar.setMaxWidth(Double.MAX_VALUE); btnSalvar.setOnAction(e -> salvarQuestao());
        Button btnCancelar = new Button("Cancelar"); btnCancelar.setMaxWidth(Double.MAX_VALUE); btnCancelar.setOnAction(e -> limparFormulario());

        painelDireito.getChildren().addAll(lblTituloForm, new Label("Enunciado/Instruções:"), txtEnunciado, new Label("Tipo de Questão:"), cbTipo, painelMultiplaEscolha, painelVerdadeiroFalso, painelLigarColunas, new Label("Imagem do Enunciado:"), new HBox(10, btnUpload, lblImagemPath), imageViewPreview, new Separator(), btnSalvar, btnCancelar);
        HBox root = new HBox(painelEsquerdo, painelDireito); HBox.setHgrow(painelEsquerdo, Priority.ALWAYS);
        primaryStage.setScene(new Scene(root, 920, 580)); primaryStage.show();
        alternarCamposPorTipo();
    }
      private void alternarCamposPorTipo() {
        String t = cbTipo.getValue();
        painelMultiplaEscolha.setVisible("Múltipla Escolha".equals(t)); painelMultiplaEscolha.setManaged("Múltipla Escolha".equals(t));
        painelVerdadeiroFalso.setVisible("Verdadeiro/Falso".equals(t)); painelVerdadeiroFalso.setManaged("Verdadeiro/Falso".equals(t));
        painelLigarColunas.setVisible("Ligar Colunas".equals(t)); painelLigarColunas.setManaged("Ligar Colunas".equals(t));
    }

    private void carregarQuestaoParaEdicao() {
        Questao q = tabela.getSelectionModel().getSelectedItem(); if (q == null) return;
        questaoEmEdicao = q; txtEnunciado.setText(q.getEnunciado()); cbTipo.setValue(q.getTipo()); alternarCamposPorTipo();
        listaAlternativasForm.clear(); listaParesForm.clear(); gabaritoMultiplaSelecionado = ""; lblGabaritoMultipla.setText("Gabarito: Não definido");

        if ("Múltipla Escolha".equals(q.getTipo())) {
            listaAlternativasForm.addAll(q.getAlternativas()); gabaritoMultiplaSelecionado = q.getRespostaCorreta(); lblGabaritoMultipla.setText("Gabarito: " + q.getRespostaCorreta());
        } else if ("Verdadeiro/Falso".equals(q.getTipo())) {
            if ("Verdadeiro".equals(q.getRespostaCorreta())) rbVerdadeiro.setSelected(true); else rbFalso.setSelected(true);
        } else if ("Ligar Colunas".equals(q.getTipo())) { listaParesForm.addAll(q.getPares()); }
        if (q.getImagemPath() != null) { 
            File f = new File(q.getImagemPath()); 
            if (f.exists()) { 
                imagemSelecionada = f; 
                lblImagemPath.setText(f.getName()); 
                imageViewPreview.setImage(new Image(f.toURI().toString())); 
            } 
        } else { 
            removerImagemPreview(); 
        }
    }

    private void salvarQuestao() {
        String enunciado = txtEnunciado.getText().trim(); 
        String tipo = cbTipo.getValue(); 
        String path = (imagemSelecionada != null) ? imagemSelecionada.getAbsolutePath() : null;
        
        if (enunciado.isEmpty()) { 
            new Alert(Alert.AlertType.ERROR, "O enunciado não pode ser vazio.").showAndWait(); 
            return; 
        }

        String gabarito = ""; 
        List<String> alts = null; 
        List<ParColuna> pares = null;
        
        if ("Múltipla Escolha".equals(tipo)) {
            if (gabaritoMultiplaSelecionado.isEmpty()) { 
                new Alert(Alert.AlertType.ERROR, "Selecione a alternativa correta.").showAndWait(); 
                return; 
            }
            gabarito = gabaritoMultiplaSelecionado; 
            alts = new ArrayList<>(listaAlternativasForm);
        } else if ("Verdadeiro/Falso".equals(tipo)) {
            gabarito = rbVerdadeiro.isSelected() ? "Verdadeiro" : "Falso";
        } else if ("Ligar Colunas".equals(tipo)) {
            if (listaParesForm.isEmpty()) { 
                new Alert(Alert.AlertType.ERROR, "Adicione ao menos um par de colunas.").showAndWait(); 
                return; 
            }
            pares = new ArrayList<>(listaParesForm);
        }

        if (questaoEmEdicao == null) { 
            listaQuestoes.add(new Questao(enunciado, tipo, path, alts, gabarito, pares)); 
        } else {
            questaoEmEdicao.setEnunciado(enunciado); 
            questaoEmEdicao.setTipo(tipo); 
            questaoEmEdicao.setImagemPath(path);
            questaoEmEdicao.setAlternativas(alts); 
            questaoEmEdicao.setRespostaCorreta(gabarito); 
            questaoEmEdicao.setPares(pares); 
            tabela.refresh();
        }
        limparFormulario();
    }

    private void limparFormulario() {
        questaoEmEdicao = null; 
        txtEnunciado.clear(); 
        cbTipo.setValue("Múltipla Escolha"); 
        listaAlternativasForm.clear(); 
        listaParesForm.clear();
        gabaritoMultiplaSelecionado = ""; 
        lblGabaritoMultipla.setText("Gabarito: Não definido"); 
        rbVerdadeiro.setSelected(true); 
        removerImagemPreview(); 
        alternarCamposPorTipo();
    }

    private void removerImagemPreview() { 
        imagemSelecionada = null; 
        lblImagemPath.setText("Nenhuma imagem selecionada"); 
        imageViewPreview.setImage(null); 
    }

    public static void main(String[] args) { 
        launch(args); 
    }
}
