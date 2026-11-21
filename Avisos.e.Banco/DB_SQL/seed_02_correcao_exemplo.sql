-- =====================================================================================
-- Dados de EXEMPLO para testar a Correção (PB-03 / PB-08). Opcional: só para desenvolvimento.
-- Rode depois da migracao_02_correcao.sql. Não roda duas vezes (confere o título da prova).
--
-- Cria: turma "Turma Correção (exemplo)", alunos Ana (já existe), Bruno e Carla (senha fictícia),
-- e a prova "Prova de Exemplo - Correção" com 4 questões (múltipla escolha, V/F, associação,
-- discursiva), prazo já encerrado e provas entregues pela Ana e pelo Bruno. A Carla não entregou.
-- =====================================================================================

DO $$
DECLARE
    prof int; turma int; av int; ana int; bruno int; carla int;
    q1 int; q2 int; q3 int; q4 int;
    a1_certa int; a1_errada int; a2_certa int; a2_errada int;
    t_ana int; t_bruno int;
BEGIN
    IF EXISTS (SELECT 1 FROM avaliacoes WHERE titulo = 'Prova de Exemplo - Correção') THEN
        RAISE NOTICE 'Seed já aplicado; nada a fazer.';
        RETURN;
    END IF;

    SELECT id INTO prof FROM usuarios WHERE perfil = 'professor' ORDER BY id LIMIT 1;
    IF prof IS NULL THEN RAISE EXCEPTION 'Cadastre um professor antes.'; END IF;

    INSERT INTO usuarios (nome, email, senha_hash, perfil)
    VALUES ('Ana Maria', 'ana@aluno.test.com', 'exemplo', 'aluno') ON CONFLICT (email) DO NOTHING;
    INSERT INTO usuarios (nome, email, senha_hash, perfil)
    VALUES ('Bruno Lima', 'bruno@aluno.test.com', 'exemplo', 'aluno') ON CONFLICT (email) DO NOTHING;
    INSERT INTO usuarios (nome, email, senha_hash, perfil)
    VALUES ('Carla Souza', 'carla@aluno.test.com', 'exemplo', 'aluno') ON CONFLICT (email) DO NOTHING;
    SELECT id INTO ana   FROM usuarios WHERE email = 'ana@aluno.test.com';
    SELECT id INTO bruno FROM usuarios WHERE email = 'bruno@aluno.test.com';
    SELECT id INTO carla FROM usuarios WHERE email = 'carla@aluno.test.com';

    INSERT INTO turmas (nome, professor_id) VALUES ('Turma Correção (exemplo)', prof) RETURNING id INTO turma;
    INSERT INTO matriculas (turma_id, aluno_id) VALUES (turma, ana), (turma, bruno), (turma, carla);

    INSERT INTO avaliacoes (turma_id, titulo, liberada, liberada_em, prazo_fim, inicio, duracao_minutos)
    VALUES (turma, 'Prova de Exemplo - Correção', true, now() - interval '10 days',
            now() - interval '1 day', now() - interval '9 days', 60) RETURNING id INTO av;

    -- Q1 múltipla escolha
    INSERT INTO questoes (professor_id, tipo, enunciado) VALUES (prof, 'multipla_escolha', 'Qual é a capital da França?') RETURNING id INTO q1;
    INSERT INTO alternativas (questao_id, texto, correta) VALUES (q1, 'Paris', true) RETURNING id INTO a1_certa;
    INSERT INTO alternativas (questao_id, texto, correta) VALUES (q1, 'Lyon', false) RETURNING id INTO a1_errada;
    INSERT INTO alternativas (questao_id, texto, correta) VALUES (q1, 'Nice', false);

    -- Q2 verdadeiro/falso
    INSERT INTO questoes (professor_id, tipo, enunciado) VALUES (prof, 'verdadeiro_falso', 'O Sol é uma estrela.') RETURNING id INTO q2;
    INSERT INTO alternativas (questao_id, texto, correta) VALUES (q2, 'Verdadeiro', true) RETURNING id INTO a2_certa;
    INSERT INTO alternativas (questao_id, texto, correta) VALUES (q2, 'Falso', false) RETURNING id INTO a2_errada;

    -- Q3 associação (uma linha por par)
    INSERT INTO questoes (professor_id, tipo, enunciado) VALUES (prof, 'associacao', 'Associe cada linguagem ao seu tipo.') RETURNING id INTO q3;
    INSERT INTO alternativas (questao_id, texto, correta, par_associacao) VALUES
        (q3, 'Java', true, 'Compilada'), (q3, 'Python', true, 'Interpretada'), (q3, 'SQL', true, 'Declarativa');

    -- Q4 discursiva (a alternativa guarda a resposta esperada)
    INSERT INTO questoes (professor_id, tipo, enunciado) VALUES (prof, 'discursiva', 'Explique o que é uma chave primária.') RETURNING id INTO q4;
    INSERT INTO alternativas (questao_id, texto, correta) VALUES (q4, 'Identifica cada linha da tabela de forma única.', true);

    -- Valores: 2,5 + 2,5 + 2,5 + 2,5 = 10
    INSERT INTO avaliacao_questoes (avaliacao_id, questao_id, ordem, valor) VALUES
        (av, q1, 1, 2.5), (av, q2, 2, 2.5), (av, q3, 3, 2.5), (av, q4, 4, 2.5);

    -- Ana: tudo certo, exceto 1 par da associação
    INSERT INTO tentativas_avaliacao (avaliacao_id, aluno_id, status, data_envio)
    VALUES (av, ana, 'enviada', now() - interval '2 days') RETURNING id INTO t_ana;
    INSERT INTO respostas_aluno (tentativa_id, questao_id, alternativa_escolhida_id, resposta_discursiva, resposta_associacao) VALUES
        (t_ana, q1, a1_certa, NULL, NULL),
        (t_ana, q2, a2_certa, NULL, NULL),
        (t_ana, q3, NULL, NULL, 'Java → Compilada; Python → Interpretada; SQL → Compilada'),
        (t_ana, q4, NULL, 'É o campo que não repete e identifica o registro.', NULL);

    -- Bruno: erra a Q1 e a Q2
    INSERT INTO tentativas_avaliacao (avaliacao_id, aluno_id, status, data_envio)
    VALUES (av, bruno, 'enviada', now() - interval '2 days') RETURNING id INTO t_bruno;
    INSERT INTO respostas_aluno (tentativa_id, questao_id, alternativa_escolhida_id, resposta_discursiva, resposta_associacao) VALUES
        (t_bruno, q1, a1_errada, NULL, NULL),
        (t_bruno, q2, a2_errada, NULL, NULL),
        (t_bruno, q3, NULL, NULL, 'Java → Compilada; Python → Interpretada; SQL → Declarativa'),
        (t_bruno, q4, NULL, 'Não sei.', NULL);

    RAISE NOTICE 'Seed criado: avaliação id %', av;
END $$;
