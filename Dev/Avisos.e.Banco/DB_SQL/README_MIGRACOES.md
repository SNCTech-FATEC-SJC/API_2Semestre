# Aplicação das migrações do banco

Depois de restaurar `snctech_backup.sql`, execute os scripts abaixo, nesta ordem, no banco SNCTECH:

1. `migracao_01_integracao_avaliacoes.sql`
2. `migracao_02_correcao.sql`
3. `migracao_03_peso_e_multiplas_turmas.sql`

A terceira migração amplia a precisão da nota consolidada, cria a relação entre avaliações e várias turmas e migra as avaliações existentes para essa relação.

No formulário de criação/edição, informe uma ou mais turmas separadas por vírgula. Exemplo: `1º A, 1º B`.

A nota consolidada é calculada como **soma das notas das questões × peso da avaliação**. As notas individuais das questões continuam armazenadas sem ponderação; somente o total consolidado recebe o peso, evitando aplicar o peso mais de uma vez quando houver correção manual posterior.

Faça backup do banco antes de executar migrações em um ambiente com dados reais.
