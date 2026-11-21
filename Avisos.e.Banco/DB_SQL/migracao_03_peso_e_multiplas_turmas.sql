BEGIN;

-- SNCTECH: suporte à nota ponderada e à associação de uma avaliação a várias turmas.
-- Executar uma única vez no banco PostgreSQL do projeto.

ALTER TABLE tentativas_avaliacao
    ALTER COLUMN nota_total TYPE numeric(10,2);

CREATE TABLE IF NOT EXISTS avaliacao_turmas (
    avaliacao_id integer NOT NULL REFERENCES avaliacoes(id) ON DELETE CASCADE,
    turma_id integer NOT NULL REFERENCES turmas(id) ON DELETE CASCADE,
    PRIMARY KEY (avaliacao_id, turma_id)
);

-- Migra avaliações antigas: a turma atualmente registrada passa a ser a primeira associação.
INSERT INTO avaliacao_turmas (avaliacao_id, turma_id)
SELECT id, turma_id
FROM avaliacoes
WHERE turma_id IS NOT NULL
ON CONFLICT DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_avaliacao_turmas_turma ON avaliacao_turmas(turma_id);
CREATE INDEX IF NOT EXISTS idx_avaliacao_turmas_avaliacao ON avaliacao_turmas(avaliacao_id);

COMMIT;
