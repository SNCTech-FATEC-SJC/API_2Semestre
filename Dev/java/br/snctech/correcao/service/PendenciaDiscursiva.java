package br.snctech.correcao.service;

import java.math.BigDecimal;

/** Item da lista de correção manual (tela "Correção manual do aluno"). */
public record PendenciaDiscursiva(Integer idAvaliacaoRespondida,
                                  Integer idAluno,
                                  Integer idQuestao,
                                  String textoResposta,
                                  String respostaEsperada,
                                  BigDecimal notaSugerida,
                                  BigDecimal valorMaximo) {}
