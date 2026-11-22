BEGIN;

CREATE TABLE IF NOT EXISTS avaliacao_alunos (
    avaliacao_id integer NOT NULL REFERENCES avaliacoes(id) ON DELETE CASCADE,
    aluno_id integer NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    PRIMARY KEY (avaliacao_id, aluno_id)
);

CREATE INDEX IF NOT EXISTS idx_avaliacao_alunos_avaliacao
    ON avaliacao_alunos(avaliacao_id);
CREATE INDEX IF NOT EXISTS idx_avaliacao_alunos_aluno
    ON avaliacao_alunos(aluno_id);

COMMIT;
