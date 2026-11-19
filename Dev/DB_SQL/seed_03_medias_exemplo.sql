DO $$
DECLARE
    turma int; av int; ana int; bruno int; carla int;
    q1 int; q2 int; a1_certa int; a1_errada int; a2_certa int;
    t_ana int; t_bruno int;
BEGIN
    IF EXISTS (SELECT 1 FROM avaliacoes WHERE titulo = 'Prova 2 - Média (exemplo)') THEN
        RAISE NOTICE 'Seed 03 já aplicado; nada a fazer.';
        RETURN;
    END IF;

    SELECT id INTO turma FROM turmas WHERE nome = 'Turma Correção (exemplo)' ORDER BY id LIMIT 1;
    IF turma IS NULL THEN RAISE EXCEPTION 'Rode antes o seed_02_correcao_exemplo.sql.'; END IF;
    SELECT id INTO ana   FROM usuarios WHERE email = 'ana@aluno.test.com';
    SELECT id INTO bruno FROM usuarios WHERE email = 'bruno@aluno.test.com';
    SELECT id INTO carla FROM usuarios WHERE email = 'carla@aluno.test.com';

    SELECT q.id INTO q1 FROM questoes q JOIN avaliacao_questoes aq ON aq.questao_id = q.id
        JOIN avaliacoes a ON a.id = aq.avaliacao_id
        WHERE a.titulo = 'Prova de Exemplo - Correção' AND q.tipo = 'multipla_escolha' LIMIT 1;
    SELECT q.id INTO q2 FROM questoes q JOIN avaliacao_questoes aq ON aq.questao_id = q.id
        JOIN avaliacoes a ON a.id = aq.avaliacao_id
        WHERE a.titulo = 'Prova de Exemplo - Correção' AND q.tipo = 'verdadeiro_falso' LIMIT 1;
    SELECT id INTO a1_certa  FROM alternativas WHERE questao_id = q1 AND correta ORDER BY id LIMIT 1;
    SELECT id INTO a1_errada FROM alternativas WHERE questao_id = q1 AND NOT correta ORDER BY id LIMIT 1;
    SELECT id INTO a2_certa  FROM alternativas WHERE questao_id = q2 AND correta ORDER BY id LIMIT 1;

    INSERT INTO avaliacoes (turma_id, titulo, peso, liberada, liberada_em, prazo_fim, inicio, duracao_minutos)
    VALUES (turma, 'Prova 2 - Média (exemplo)', 2.0, true, now() - interval '8 days',
            now() - interval '1 day', now() - interval '7 days', 30) RETURNING id INTO av;

    INSERT INTO avaliacao_questoes (avaliacao_id, questao_id, ordem, valor) VALUES
        (av, q1, 1, 5.0), (av, q2, 2, 5.0);

    INSERT INTO tentativas_avaliacao (avaliacao_id, aluno_id, status, data_envio)
    VALUES (av, ana, 'enviada', now() - interval '2 days') RETURNING id INTO t_ana;
    INSERT INTO respostas_aluno (tentativa_id, questao_id, alternativa_escolhida_id) VALUES
        (t_ana, q1, a1_certa), (t_ana, q2, a2_certa);

    INSERT INTO tentativas_avaliacao (avaliacao_id, aluno_id, status, data_envio)
    VALUES (av, bruno, 'enviada', now() - interval '2 days') RETURNING id INTO t_bruno;
    INSERT INTO respostas_aluno (tentativa_id, questao_id, alternativa_escolhida_id) VALUES
        (t_bruno, q1, a1_errada), (t_bruno, q2, a2_certa);

    RAISE NOTICE 'Seed 03 criado: avaliação id %', av;
END $$;
