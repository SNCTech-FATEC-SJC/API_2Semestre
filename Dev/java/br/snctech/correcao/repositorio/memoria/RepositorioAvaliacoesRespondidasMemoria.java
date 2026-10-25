package br.snctech.correcao.repositorio.memoria;

import br.snctech.correcao.model.AvaliacaoRespondida;
import br.snctech.correcao.model.RespostaQuestao;
import br.snctech.correcao.model.StatusCorrecao;
import br.snctech.correcao.repositorio.RepositorioAvaliacoesRespondidas;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provas respondidas guardadas em memória, com dados de demonstração (seed):
 * <ul>
 *   <li>prova 1 – João: tem discursivas pendentes (situação "Pendente");</li>
 *   <li>prova 2 – Maria: já 100% corrigida (situação "Corrigida");</li>
 *   <li>Pedro não tem prova (situação "Não respondeu").</li>
 * </ul>
 * Os dados somem ao fechar o programa; a versão com banco será feita por outro integrante.
 */
public class RepositorioAvaliacoesRespondidasMemoria implements RepositorioAvaliacoesRespondidas {

    private static final int ID_PROFESSOR_DEMO = 99;

    private final Map<Integer, AvaliacaoRespondida> provas = new LinkedHashMap<>();

    public RepositorioAvaliacoesRespondidasMemoria() {
        semear();
    }

    private void semear() {
        int avaliacao = RepositorioQuestoesCorrecaoMemoria.ID_AVALIACAO_DEMO;

        AvaliacaoRespondida joao = new AvaliacaoRespondida(1, avaliacao, 1);
        joao.adicionar(alternativa(1, "B"));                       // certa
        joao.adicionar(alternativa(2, "F"));                       // errada (gabarito é V)
        joao.adicionar(pares(3, Map.of("a1", "b1", "a2", "b3")));  // 1 de 2 pares
        joao.adicionar(texto(4, "Banco de dados relacional usa tabelas"));
        joao.adicionar(texto(5, "Java trabalha com objetos"));
        provas.put(joao.getId(), joao);

        AvaliacaoRespondida maria = new AvaliacaoRespondida(2, avaliacao, 2);
        maria.adicionar(alternativa(1, "B"));
        maria.adicionar(alternativa(2, "V"));
        maria.adicionar(pares(3, Map.of("a1", "b1", "a2", "b2")));
        maria.adicionar(corrigidaPeloProfessor(texto(4, "Organiza os dados em tabelas ligadas por chaves"), "2.0"));
        maria.adicionar(corrigidaPeloProfessor(texto(5, "Java usa classes e objetos"), "1.5"));
        provas.put(maria.getId(), maria);
    }

    private static RespostaQuestao alternativa(int idQuestao, String alternativaId) {
        RespostaQuestao r = new RespostaQuestao(idQuestao);
        r.setAlternativaId(alternativaId);
        return r;
    }

    private static RespostaQuestao pares(int idQuestao, Map<String, String> pares) {
        RespostaQuestao r = new RespostaQuestao(idQuestao);
        r.setPares(pares);
        return r;
    }

    private static RespostaQuestao texto(int idQuestao, String texto) {
        RespostaQuestao r = new RespostaQuestao(idQuestao);
        r.setTexto(texto);
        return r;
    }

    private static RespostaQuestao corrigidaPeloProfessor(RespostaQuestao r, String nota) {
        r.setNota(new BigDecimal(nota));
        r.setStatus(StatusCorrecao.CORRIGIDA_MANUALMENTE);
        r.setCorrigidoPor(ID_PROFESSOR_DEMO);
        return r;
    }

    @Override
    public AvaliacaoRespondida buscar(Integer id) {
        AvaliacaoRespondida prova = provas.get(id);
        if (prova == null) {
            throw new IllegalArgumentException("Prova respondida não encontrada: " + id);
        }
        return prova;
    }

    @Override
    public List<AvaliacaoRespondida> buscarPorAvaliacao(Integer idAvaliacao) {
        return provas.values().stream()
                .filter(p -> p.getIdAvaliacao().equals(idAvaliacao))
                .toList();
    }

    @Override
    public void salvar(AvaliacaoRespondida avaliacaoRespondida) {
        provas.put(avaliacaoRespondida.getId(), avaliacaoRespondida);
    }
}
