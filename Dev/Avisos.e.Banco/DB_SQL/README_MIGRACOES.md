# Aplicação das migrações do banco

Depois de restaurar `snctech_backup.sql`, execute os scripts abaixo, nesta ordem, no banco SNCTECH:

1. `migracao_01_integracao_avaliacoes.sql`
2. `migracao_02_correcao.sql`
3. `seed_02_correcao_exemplo.sql`
4. `migracao_03_peso_e_multiplas_turmas.sql`
5. `migracao_04_alunos_selecionados.sql`
6. `migracao_05_notas_e_medias.sql`
7. `seed_05_medias_exemplo.sql`
8. `migracao_06_vincular_avaliacoes_seed.sql`

O programa aplica os scripts automaticamente nessa ordem. A migração 06 registra na relação `avaliacao_turmas` as avaliações criadas pelo seed 05.

No formulário de criação/edição, informe uma ou mais turmas separadas por vírgula. Exemplo: `1º A, 1º B`.

A nota consolidada é armazenada como a soma das notas das questões, sem ponderação. O peso da avaliação é aplicado somente no cálculo da média.

Faça backup do banco antes de executar migrações em um ambiente com dados reais.
