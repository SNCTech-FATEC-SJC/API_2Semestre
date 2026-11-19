BEGIN;

ALTER TABLE public.tentativas_avaliacao
    ADD COLUMN IF NOT EXISTS correcao_finalizada boolean NOT NULL DEFAULT false;

ALTER TABLE public.tentativas_avaliacao
    ADD COLUMN IF NOT EXISTS correcao_finalizada_em timestamp;

CREATE TABLE IF NOT EXISTS public.medias_finais (
    turma_id     integer      NOT NULL REFERENCES public.turmas(id)   ON DELETE CASCADE,
    aluno_id     integer      NOT NULL REFERENCES public.usuarios(id) ON DELETE CASCADE,
    media        numeric(4,2) NOT NULL CHECK (media >= 0 AND media <= 10),
    situacao     character varying(20) NOT NULL
                 CHECK (situacao IN ('APROVADO', 'EXAME_FINAL', 'REPROVADO')),
    liberada     boolean      NOT NULL DEFAULT false,
    calculada_em timestamp    NOT NULL DEFAULT now(),
    liberada_em  timestamp,
    PRIMARY KEY (turma_id, aluno_id)
);

COMMIT;
