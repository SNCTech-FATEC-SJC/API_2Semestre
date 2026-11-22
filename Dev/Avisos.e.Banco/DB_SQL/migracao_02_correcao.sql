-- =====================================================================================
-- Migração 02 - Correção manual e automática (PB-03 e PB-08)
--
-- Rode DEPOIS do snctech_backup.sql e da migracao_01_integracao_avaliacoes.sql.
-- Pode ser executada mais de uma vez. Só ACRESCENTA colunas; não altera nem apaga dados.
-- =====================================================================================

BEGIN;

-- Quanto vale cada questão dentro da avaliação (definido pelo professor; a soma deve ser 10).
-- Vazio (NULL) = a Correção divide os 10 pontos igualmente entre as questões.
ALTER TABLE public.avaliacao_questoes
    ADD COLUMN IF NOT EXISTS valor numeric(5,2);

-- Situação da correção de cada resposta:
--   CORRIGIDA_AUTOMATICAMENTE | PENDENTE_MANUAL (discursiva aguardando o professor) | CORRIGIDA_MANUALMENTE
-- Vazio (NULL) = a resposta ainda não passou pela correção.
ALTER TABLE public.respostas_aluno
    ADD COLUMN IF NOT EXISTS status_correcao character varying(30);

-- Nota sugerida pelo sistema para a discursiva (o professor aceita ou altera).
ALTER TABLE public.respostas_aluno
    ADD COLUMN IF NOT EXISTS nota_sugerida numeric(5,2);

-- Professor que lançou ou alterou a nota.
ALTER TABLE public.respostas_aluno
    ADD COLUMN IF NOT EXISTS corrigido_por integer REFERENCES public.usuarios(id) ON DELETE SET NULL;

COMMIT;
