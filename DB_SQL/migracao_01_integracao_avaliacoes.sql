-- =====================================================================================
-- Migração 01 - integração do gerenciamento de avaliações (PB-01, PB-06 e PB-07)
--
-- Rode DEPOIS de restaurar o snctech_backup.sql. Pode ser executada mais de uma vez.
-- Ela só ACRESCENTA colunas e índices; não altera nem apaga nenhum dado existente.
-- =====================================================================================

BEGIN;

-- Quando a avaliação começa (data + hora). O prazo_fim que já existia continua sendo o término.
ALTER TABLE public.avaliacoes
    ADD COLUMN IF NOT EXISTS inicio timestamp without time zone;

-- Tempo máximo que cada aluno tem para responder, em minutos (0 = só vale o prazo_fim).
ALTER TABLE public.avaliacoes
    ADD COLUMN IF NOT EXISTS duracao_minutos integer NOT NULL DEFAULT 0;

-- Momento em que o tempo daquele aluno acaba: o que vier primeiro entre
-- (data_inicio + duracao_minutos) e o prazo_fim da avaliação.
ALTER TABLE public.tentativas_avaliacao
    ADD COLUMN IF NOT EXISTS limite timestamp without time zone;

-- Cada aluno só pode abrir uma avaliação uma vez (a regra também é conferida na aplicação;
-- o índice garante isso mesmo se duas pessoas tentarem ao mesmo tempo).
CREATE UNIQUE INDEX IF NOT EXISTS ux_tentativa_avaliacao_aluno
    ON public.tentativas_avaliacao (avaliacao_id, aluno_id);

-- Uma resposta por questão em cada tentativa.
CREATE UNIQUE INDEX IF NOT EXISTS ux_resposta_tentativa_questao
    ON public.respostas_aluno (tentativa_id, questao_id);

COMMIT;
