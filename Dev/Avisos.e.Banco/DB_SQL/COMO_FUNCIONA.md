# Scripts do banco

O programa aplica estes scripts sozinho ao abrir (classe `br.snctech.banco.PreparadorBanco`). Ninguém precisa rodá-los no pgAdmin.

1. Se o banco `SNCTech_DB` não existir, ele é criado.
2. `snctech_backup.sql` roda só se o banco estiver vazio.
3. Depois rodam os `migracao_NN_*.sql` e `seed_NN_*.sql` ainda não aplicados, por número (no mesmo número, a migração vem antes do seed).
4. Cada script aplicado fica anotado na tabela `controle_scripts` e não roda de novo.

## Para criar um script novo

- Use o próximo número livre: `migracao_NN_descricao.sql` ou `seed_NN_descricao.sql`.
- Nunca altere um script que já está na `main`; crie outro.
- Faça o script poder rodar mais de uma vez sem erro (`IF NOT EXISTS`, `ON CONFLICT DO NOTHING`).
- Se o script falhar, nada dele é gravado e o programa mostra o erro.
