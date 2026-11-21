package br.snctech.correcao;

import br.snctech.correcao.model.AlunoResumo;
import br.snctech.correcao.model.AvaliacaoRespondida;
import br.snctech.correcao.model.QuestaoCorrecao;
import br.snctech.correcao.repositorio.RepositorioAlunos;
import br.snctech.correcao.repositorio.RepositorioAvaliacoesRespondidas;
import br.snctech.correcao.repositorio.RepositorioQuestoesCorrecao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Repositórios em memória: permitem testar sem banco e sem depender dos colegas. */
final class Fakes {

    private Fakes() {}

    static class RespondidaEmMemoria implements RepositorioAvaliacoesRespondidas {
        final Map<Integer, AvaliacaoRespondida> dados = new HashMap<>();

        @Override
        public AvaliacaoRespondida buscar(Integer id) {
            AvaliacaoRespondida ar = dados.get(id);
            if (ar == null) {
                throw new IllegalArgumentException("Prova respondida não encontrada: " + id);
            }
            return ar;
        }

        @Override
        public List<AvaliacaoRespondida> buscarPorAvaliacao(Integer idAvaliacao) {
            return dados.values().stream()
                    .filter(ar -> ar.getIdAvaliacao().equals(idAvaliacao))
                    .collect(Collectors.toList());
        }

        @Override
        public void salvar(AvaliacaoRespondida ar) {
            dados.put(ar.getId(), ar);
        }
    }

    static class QuestaoEmMemoria implements RepositorioQuestoesCorrecao {
        private final List<QuestaoCorrecao> questoes;

        QuestaoEmMemoria(List<QuestaoCorrecao> questoes) {
            this.questoes = questoes;
        }

        @Override
        public List<QuestaoCorrecao> buscarPorAvaliacao(Integer idAvaliacao) {
            return questoes;
        }
    }

    static class AlunoEmMemoria implements RepositorioAlunos {
        private final List<AlunoResumo> alunos;

        AlunoEmMemoria(List<AlunoResumo> alunos) {
            this.alunos = alunos;
        }

        @Override
        public List<AlunoResumo> buscarPorAvaliacao(Integer idAvaliacao) {
            return alunos;
        }
    }
}
