package br.snctech.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * Apoio para montar colunas de TableView sem PropertyValueFactory (funciona com records).
 */
public final class TabelaUtil {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private TabelaUtil() {
    }

    public static <T> TableColumn<T, String> coluna(String titulo, double largura, Function<T, String> extrator) {
        TableColumn<T, String> coluna = new TableColumn<>(titulo);
        coluna.setPrefWidth(largura);
        coluna.setCellValueFactory(dados -> new SimpleStringProperty(extrator.apply(dados.getValue())));
        return coluna;
    }

    /** Pinta a coluna de situação: "Ativa" em verde, qualquer outro valor em cinza. */
    public static <T> void estilizarSituacao(TableColumn<T, String> coluna) {
        coluna.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean vazio) {
                super.updateItem(item, vazio);
                getStyleClass().removeAll("status-ativa", "status-concluida");
                if (vazio || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                getStyleClass().add("Ativa".equals(item) ? "status-ativa" : "status-concluida");
            }
        });
    }

    public static String data(LocalDateTime valor) {
        return valor == null ? "-" : DATA_HORA.format(valor);
    }
}
