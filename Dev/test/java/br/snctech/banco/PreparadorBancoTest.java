package br.snctech.banco;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreparadorBancoTest {

    @Test
    void ordenaPorNumeroComMigracaoAntesDoSeed() {
        List<String> ordem = PreparadorBanco.scriptsEmOrdem(List.of(
                "seed_05_medias_exemplo.sql", "migracao_10_futura.sql", "seed_02_correcao_exemplo.sql",
                "migracao_05_notas_e_medias.sql", "migracao_02_correcao.sql", "migracao_01_integracao_avaliacoes.sql",
                "migracao_03_peso_e_multiplas_turmas.sql", "migracao_04_alunos_selecionados.sql"));
        assertEquals(List.of(
                "migracao_01_integracao_avaliacoes.sql", "migracao_02_correcao.sql", "seed_02_correcao_exemplo.sql",
                "migracao_03_peso_e_multiplas_turmas.sql", "migracao_04_alunos_selecionados.sql",
                "migracao_05_notas_e_medias.sql", "seed_05_medias_exemplo.sql", "migracao_10_futura.sql"), ordem);
    }

    @Test
    void ignoraArquivosForaDoPadrao() {
        List<String> ordem = PreparadorBanco.scriptsEmOrdem(List.of(
                "snctech_backup.sql", "README_MIGRACOES.md", "migracao_sem_numero.sql", "teste.sql",
                "migracao_01_ok.sql"));
        assertEquals(List.of("migracao_01_ok.sql"), ordem);
    }

    @Test
    void tiraSoOBeginECommitSoltos() {
        String sql = "﻿BEGIN;\nALTER TABLE x ADD COLUMN y int;\nDO $$\nBEGIN\n  RAISE NOTICE 'oi';\nEND $$;\n  commit ;\n";
        String limpo = PreparadorBanco.semBeginECommit(sql);
        assertFalse(limpo.startsWith("﻿"));
        assertFalse(limpo.contains("BEGIN;"));
        assertFalse(limpo.toLowerCase().contains("commit"));
        assertTrue(limpo.contains("DO $$\nBEGIN\n"));
        assertTrue(limpo.contains("ALTER TABLE x ADD COLUMN y int;"));
    }
}
