package br.snctech.notas.controller;

import br.snctech.notas.DadosNotas;
import br.snctech.notas.model.MediaLiberada;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

public final class PainelMediaDoAluno {

    private PainelMediaDoAluno() {
    }

    public static List<Node> linhas(String email) {
        List<Node> linhas = new ArrayList<>();
        Label titulo = new Label("Média final");
        titulo.getStyleClass().addAll("section-label", "strong");
        linhas.add(titulo);
        String problema = DadosNotas.verificarTabela();
        if (problema != null) {
            linhas.add(mensagem(problema));
            return linhas;
        }
        try {
            List<MediaLiberada> medias = DadosNotas.servico().mediasLiberadasDoAluno(email);
            if (medias.isEmpty()) {
                linhas.add(mensagem("A média final será exibida quando a turma for concluída e todas as avaliações estiverem corrigidas."));
                return linhas;
            }
            for (MediaLiberada media : medias) {
                Label linha = new Label(media.turma() + "  •  Média final: " + media.textoDaMedia()
                        + "  •  Situação: " + media.situacao().rotulo());
                linha.setWrapText(true);
                linhas.add(linha);
            }
        } catch (RuntimeException erro) {
            linhas.add(mensagem("Não foi possível carregar a média final. Detalhe: " + erro.getMessage()));
        }
        return linhas;
    }

    private static Label mensagem(String texto) {
        Label label = new Label(texto);
        label.setWrapText(true);
        label.getStyleClass().add("mensagem");
        return label;
    }
}
