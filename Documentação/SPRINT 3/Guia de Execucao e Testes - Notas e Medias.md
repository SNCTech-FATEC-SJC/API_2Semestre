# Guia de Execução e Testes — Correção, Notas e Média Final

Este guia mostra, do zero, como **abrir e rodar o projeto no IntelliJ** (o banco é montado pelo próprio programa) e **testar** a parte de Correção, Notas e Média Final (PB-03, PB-08, PB-12 a PB-17).

| Item | Valor |
|---|---|
| Branch | `notas_e_media_final` |
| Java | JDK 25 |
| JavaFX | 21.0.2 (baixado pelo Maven) |
| Banco | PostgreSQL 18 (o pgAdmin é opcional) |
| Nome do banco | `SNCTech_DB` |
| Pasta do projeto | `Dev` (onde fica o `pom.xml`) |

---

## Sumário

1. [Pré-requisitos](#1-pré-requisitos)
2. [Baixar o código](#2-baixar-o-código)
3. [Banco de dados: o programa prepara sozinho](#3-banco-de-dados-o-programa-prepara-sozinho)
4. [Trocar a senha salva](#4-trocar-a-senha-salva)
5. [Abrir e rodar no IntelliJ](#5-abrir-e-rodar-no-intellij)
6. [Roteiro de testes](#6-roteiro-de-testes)
7. [Como recomeçar os testes do zero](#7-como-recomeçar-os-testes-do-zero)
8. [Problemas comuns](#8-problemas-comuns)

---

## 1. Pré-requisitos

- **PostgreSQL 18** instalado com: PostgreSQL Server, pgAdmin 4 e Command Line Tools. Anote a senha do usuário `postgres` escolhida na instalação.
- **JDK 25** instalado.
- **IntelliJ IDEA** (Community ou Ultimate).
- **Git** instalado.

---

## 2. Baixar o código

No terminal (Git Bash), na pasta onde quer guardar o projeto:

```bash
git clone https://github.com/SNCTech-FATEC-SJC/API_2Semestre.git
cd API_2Semestre
git checkout notas_e_media_final
```

Se você já tem o repositório clonado:

```bash
git fetch
git checkout notas_e_media_final
git pull
```

---

## 3. Banco de dados: o programa prepara sozinho

Não é preciso rodar nenhum script no pgAdmin nem criar o `banco.properties` à mão. Ao abrir, o programa:

1. **Pede a senha do PostgreSQL** (só na primeira vez). É a senha escolhida na instalação. Ela fica salva no arquivo `Dev/banco.properties`, que **não vai para o Git**.
2. **Cria o banco `SNCTech_DB`** se ele ainda não existir.
3. **Roda os scripts da pasta `Dev/Avisos.e.Banco/DB_SQL`** que ainda não foram aplicados naquele computador, na ordem certa, e anota cada um na tabela `controle_scripts`. Cada script roda **uma única vez**.

Ordem em que os scripts são aplicados:

| Ordem | Arquivo | O que faz |
|---|---|---|
| 1 | `snctech_backup.sql` | Cria as tabelas e os usuários de teste (só se o banco estiver vazio) |
| 2 | `migracao_01_integracao_avaliacoes.sql` | Ajustes de avaliações e tentativas |
| 3 | `migracao_02_correcao.sql` | Colunas da correção |
| 4 | `seed_02_correcao_exemplo.sql` | Turma de exemplo, alunos Ana, Bruno e Carla e a 1ª prova |
| 5 | `migracao_05_notas_e_medias.sql` | Coluna "correção finalizada" e tabela `medias_finais` |
| 6 | `seed_05_medias_exemplo.sql` | 2ª prova de exemplo (peso 2) |

> As migrações 03 e 04 são da branch de Disponibilização de provas. Quando ela entrar na `main`, o programa aplica as duas sozinho.

### 3.1 Regra para quem criar um script novo

- Nome no formato `migracao_NN_descricao.sql` (estrutura) ou `seed_NN_descricao.sql` (dados de exemplo), com `NN` maior que o último número usado.
- Os scripts rodam por número; no mesmo número, a migração roda antes do seed.
- **Nunca altere um script que já foi para a `main`**: crie um novo. Quem já rodou o antigo não roda de novo.
- Escreva o script para poder rodar mais de uma vez sem erro (`IF NOT EXISTS`, `ON CONFLICT DO NOTHING`).
- Basta colocar o arquivo na pasta: na próxima vez que cada colega abrir o programa, o banco dele se atualiza.

### 3.2 Conferir (opcional, no pgAdmin)

```sql
SELECT arquivo, aplicado_em FROM controle_scripts ORDER BY aplicado_em;
SELECT titulo, peso FROM avaliacoes ORDER BY id;
```

Na segunda consulta devem aparecer **Prova de Exemplo - Correção (1.00)** e **Prova 2 - Média (exemplo) (2.00)**.

---

## 4. Trocar a senha salva

Se a senha do PostgreSQL mudar, o programa avisa que ela não funcionou e pede de novo. Para trocar à mão, apague o arquivo `Dev/banco.properties` e abra o programa.

---

## 5. Abrir e rodar no IntelliJ

### 5.1 Abrir o projeto

1. **File → Open** e escolha a pasta **`Dev`** (a que tem o `pom.xml`), não a pasta `API_2Semestre`.
2. Se perguntar *Trust project?*, clique em **Trust Project**.
3. Espere o Maven baixar as dependências (barra no canto inferior direito).
4. Confira o Java: **File → Project Structure → Project → SDK = 25**.
5. Confira a branch no alto da janela: `notas_e_media_final`.

### 5.2 Rodar os testes automáticos

1. Abra o painel **Maven** (ícone **m** na barra da direita).
2. Clique em **Reload** (↻).
3. **snctech → Lifecycle → `test`** (dois cliques).
4. Resultado esperado no final do console:

```
Tests run: 115, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

> Rode o programa uma vez **antes** dos testes, para o `banco.properties` ser criado. Se aparecer `Skipped` maior que 0, os testes de integração com o banco foram pulados porque o programa ainda não preparou o banco.

### 5.3 Rodar o programa

1. No painel **Maven**: **snctech → Plugins → javafx → `javafx:run`** (dois cliques).
2. Na primeira vez, digite a **senha do PostgreSQL** e clique em **Conectar**. O programa cria o banco e roda os scripts (leva poucos segundos).
3. O painel do professor abre.

> **Não** rode o `App.java` pelo ▶ verde do editor: isso dá o erro *"JavaFX runtime components are missing"*. Use sempre o `javafx:run`.

---

## 6. Roteiro de testes

Usa os dados de exemplo (seeds). Cada passo tem a **ação** e o **resultado esperado**.

### Logins de teste

| Perfil | E-mail | Senha |
|---|---|---|
| Professor | `carlos@test.com` | `987654` |
| Aluna | `ana@aluno.test.com` | `testaluno` |
| Aluno | `bruno@aluno.test.com` | `exemplo` |
| Aluna | `carla@aluno.test.com` | `exemplo` |

O programa abre no painel do professor. Para **ver como aluno**: **Sair da conta → Sim**, entre com o e-mail e a senha do aluno e clique em **Minhas Notas**. Para voltar ao professor: **Sair da conta** e entre com `carlos@test.com`.

> Atalho, sem trocar de login: no professor, **Provas Aplicadas → Visualização do aluno**, digite o e-mail do aluno e clique em **Ver minhas notas**.

### Parte 1 — Antes de o professor finalizar a correção

| # | Ação | Resultado esperado |
|---|---|---|
| 1 | Painel do professor → **Notas e Médias** | Turma **"Turma Correção (exemplo)"** selecionada. Colunas "Prova de Exemplo - Correção / peso 1" e "Prova 2 - Média (exemplo) / peso 2". Ana e Bruno com **"Aguardando Correção do Professor"** nas duas provas e média **"–"**. Carla com **"0,00 (não realizada)"** nas duas provas e média **0,00 • Reprovado** (ela não fez nenhuma prova). Mensagem listando as pendências e botão **Liberar médias desativado**. |
| 2 | Ver como a **Ana** (login `ana@aluno.test.com` → **Minhas Notas**) | As duas provas com **"Aguardando Correção do Professor"**. Média final: *"A média final ainda não foi liberada pelo professor."* |

### Parte 2 — Correção e finalização pelo professor

| # | Ação | Resultado esperado |
|---|---|---|
| 3 | Provas Aplicadas → clicar no cartão **Prova de Exemplo - Correção** → **Corrigir prova** | Ana e Bruno com selo **"Pendente"** (1 discursiva pendente) e botão **Corrigir**. Carla com **"Não respondeu"**. |
| 4 | Clicar em **Corrigir** na linha da **Ana** | Selo **"Parcial"** e botão **Finalizar correção desativado**. |
| 5 | Na discursiva da Ana, digitar **2** → **Alterar nota** | Selo **"Falta finalizar"** (azul), nota **8,67** e botão **Finalizar correção ativo**. |
| 6 | Clicar em **Finalizar correção** | Selo **"Finalizada"** (verde). |
| 7 | Ver como a **Ana** | Prova de Exemplo com nota **8,67**. Prova 2 ainda **"Aguardando Correção do Professor"**. |
| 8 | **Voltar à lista de alunos** (a Ana aparece **"Finalizada"**, botão **Revisar**) → **Corrigir** na linha do **Bruno** → nota **2** na discursiva → **Finalizar correção** | Nota do Bruno **4,50** e selo **"Finalizada"**. |
| 9 | Provas Aplicadas → cartão **Prova 2 - Média (exemplo)** → **Corrigir prova** → **Finalizar** na linha da **Ana** → **Finalizar correção**; depois escolher **Bruno** no seletor do alto → **Finalizar correção** | A prova já veio corrigida pelo sistema (só objetivas): na lista, os dois aparecem **"Falta finalizar"**. Ana **10,00** e Bruno **5,00**, ambos **"Finalizada"** no fim. |

### Parte 3 — Média final e liberação

| # | Ação | Resultado esperado |
|---|---|---|
| 10 | Painel → **Notas e Médias** | Tabela abaixo e botão **Liberar médias ativo**. |
| 11 | Ver como a **Ana** | As duas notas aparecem, mas a Média final diz **"ainda não foi liberada"**. |
| 12 | Notas e Médias → **Liberar médias** → **OK** | Coluna "Para o aluno" = **"Liberada"**, mensagem *"Médias liberadas..."* e botão desativado. |
| 13 | Ver como a **Ana** e depois como o **Bruno** | Ana: **Média final 9,56 • Aprovado**. Bruno: **4,83 • Exame final**. |

Tabela esperada no passo 10:

| Aluno | Prova de Exemplo (peso 1) | Prova 2 (peso 2) | Média final | Situação |
|---|---|---|---|---|
| Ana Maria | 8,67 | 10,00 | **9,56** | Aprovado |
| Bruno Lima | 4,50 | 5,00 | **4,83** | Exame final |
| Carla Souza | 0,00 (não realizada) | 0,00 (não realizada) | **0,00** | Reprovado |

Conta da Ana: (8,67 × 1 + 10,00 × 2) ÷ (1 + 2) = 28,67 ÷ 3 = **9,56**.

### Parte 4 — Recálculo automático

| # | Ação | Resultado esperado |
|---|---|---|
| 14 | Corrigir a Prova de Exemplo → **Ana** → mudar a discursiva para **0,50** → **Alterar nota** | Nota da Ana na prova passa a **7,17**. |
| 15 | Painel → **Notas e Médias** | Média da Ana **9,06**, continua **Liberada**. Vendo como a Ana, a média é **9,06**. |

### Regras conferidas por este roteiro

- O aluno só vê a nota depois que o professor **finaliza a correção** daquela prova (passos 2, 7 e 11).
- O professor só consegue finalizar quando **todas as questões têm nota** (passo 4).
- A média final é **ponderada pelos pesos** das provas (passo 10).
- Quem não fez a prova até o fim do prazo fica com **0** (Carla).
- Situação: **6 ou mais = Aprovado; de 4 até menos de 6 = Exame final; menos de 4 = Reprovado**.
- A média só pode ser liberada quando **todas** as provas da turma estão finalizadas (passos 1 e 10).
- Se uma média já liberada voltar a ficar pendente (por exemplo, uma prova nova na turma), ela some para o aluno até tudo ser finalizado e o professor **liberar de novo**.
- O aluno só vê a média depois da **liberação** (passos 11 e 13).
- Uma nota alterada depois da liberação **recalcula a média automaticamente** (passos 14 e 15).

---

## 7. Como recomeçar os testes do zero

O roteiro altera os dados do banco. Para repetir:

1. Feche o programa.
2. No pgAdmin, botão direito em **`SNCTech_DB` → Delete (Force)** e confirme.
3. Abra o programa de novo (`javafx:run`): ele cria o banco e roda todos os scripts outra vez.

---

## 8. Problemas comuns

| Problema | Causa | Solução |
|---|---|---|
| Janela pedindo a senha toda vez | Senha errada | Digite a senha escolhida na instalação do PostgreSQL. Se esqueceu, troque pelo pgAdmin: `ALTER USER postgres WITH PASSWORD 'nova';` |
| *Não foi possível falar com o PostgreSQL* | PostgreSQL desligado ou não instalado | No Windows: **Serviços → postgresql-x64-18 → Iniciar**. |
| *Erro ao aplicar o script X* | Erro dentro daquele script | Nada dele foi gravado. Corrija o script (ou avise quem o criou) e abra o programa de novo. |
| *O banco está vazio e a pasta Avisos.e.Banco/DB_SQL não foi encontrada* | Programa rodado fora da pasta `Dev` | Abra a pasta `Dev` no IntelliJ e rode pelo `javafx:run`. |
| *JavaFX runtime components are missing* | Programa rodado pelo ▶ do editor | Rode pelo Maven: **Plugins → javafx → javafx:run**. |
| *UnsupportedClassVersionError* | O Maven está usando um Java antigo | Configure o JDK 25 no projeto e no `JAVA_HOME`. |
| Notas e Médias diz *"Você ainda não tem turmas cadastradas"* | Os seeds foram removidos da tabela `controle_scripts` ou o banco é antigo | Recomece do zero (seção 7). |
| A Ana não aparece em Notas e Médias com as médias esperadas | Nota diferente de **2** nas discursivas | Use 2 nos passos 5 e 8, ou recomece do zero (seção 7). |
