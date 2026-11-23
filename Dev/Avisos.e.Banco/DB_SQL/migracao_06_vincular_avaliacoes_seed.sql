BEGIN;

INSERT INTO avaliacao_turmas (avaliacao_id, turma_id)
SELECT id, turma_id
FROM avaliacoes
WHERE turma_id IS NOT NULL
ON CONFLICT DO NOTHING;

COMMIT;
