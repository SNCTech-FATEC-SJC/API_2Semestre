# Guia de Execução e Testes — Correção, Notas e Média Final

Este guia mostra, do zero, como **montar o banco no pgAdmin**, **abrir e rodar o projeto no IntelliJ** e **testar** a parte de Correção, Notas e Média Final (PB-03, PB-08, PB-12 a PB-17).

| Item | Valor |
|---|---|
| Branch | `notas_e_media_final` |
| Java | JDK 25 |
| JavaFX | 21.0.2 (baixado pelo Maven) |
| Banco | PostgreSQL 18 + pgAdmin 4 |
| Nome do banco | `SNCTech_DB` |
| Pasta do projeto | `Dev` (onde fica o `pom.xml`) |

---

## Sumário

1. [Pré-requisitos](#1-pré-requisitos)
2. [Baixar o código](#2-baixar-o-código)
3. [Montar o banco no pgAdmin](#3-montar-o-banco-no-pgadmin)
4. [Configurar o banco.properties](#4-configurar-o-bancoproperties)
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

## 3. Montar o banco no pgAdmin

### 3.1 Criar o banco

1. Abra o **pgAdmin 4** e conecte no servidor **PostgreSQL 18** (digite a senha do `postgres` se pedir).
2. Clique com o botão direito em **Databases → Create → Database…**
3. Em **Database**, digite exatamente: `SNCTech_DB`
4. Clique em **Save**.

> Se o banco `SNCTech_DB` já existir e você quiser começar do zero, veja a [seção 7](#7-como-recomeçar-os-testes-do-zero).

### 3.2 Como rodar um arquivo .sql (Query Tool)

Repita este procedimento para **cada** arquivo da tabela abaixo:

1. Na árvore da esquerda, clique em **`SNCTech_DB`** para selecioná-lo.
2. Menu **Tools → Query Tool**. Confira o título da aba: tem que ser `SNCTech_DB/postgres@PostgreSQL 18`.
3. Clique no ícone de **pasta** 📁 (Open File), primeiro da barra da aba.
4. Abra o arquivo, que fica em `API_2Semestre\Dev\DB_SQL\`.
5. Aperte **F5** (Execute script).
6. Confira a aba **Messages**: deve terminar com *Query returned successfully*.

> **Use uma aba nova para cada arquivo.** O backup altera configurações da sessão; rodar o próximo script na mesma aba pode dar erro de "relation does not exist".

### 3.3 Ordem dos arquivos

| Ordem | Arquivo | O que faz | Mensagem esperada |
|---|---|---|---|
| 1 | `snctech_backup.sql` | Cria as tabelas e os usuários de teste | `Query returned successfully` |
| 2 | `migracao_01_integracao_avaliacoes.sql` | Ajustes de avaliações e tentativas | `COMMIT` |
| 3 | `migracao_02_correcao.sql` | Colunas da correção | `COMMIT` |
| 4 | `seed_02_correcao_exemplo.sql` | Turma de exemplo, alunos Ana, Bruno e Carla e a 1ª prova | `Seed criado...` |
| 5 | `migracao_03_notas_e_medias.sql` | Coluna "correção finalizada" e tabela `medias_finais` | `COMMIT` |
| 6 | `seed_03_medias_exemplo.sql` | 2ª prova de exemplo (peso 2) | `Seed 03 criado: avaliação id ...` |

- Os arquivos **1, 2, 3 e 5** são obrigatórios.
- Os arquivos **4 e 6** (seeds) são **dados de exemplo**, usados no roteiro de testes e na apresentação.
- As migrações e os seeds podem ser rodados mais de uma vez sem estragar nada.

### 3.4 Conferir

Numa aba nova do Query Tool (no `SNCTech_DB`), rode:

```sql
SELECT titulo, peso FROM avaliacoes ORDER BY id;
```

Resultado esperado:

| titulo | peso |
|---|---|
| Prova de Exemplo - Correção | 1.00 |
| Prova 2 - Média (exemplo) | 2.00 |

---

## 4. Configurar o banco.properties

O arquivo `banco.properties` guarda a senha do **seu** PostgreSQL. Ele **não vai para o Git** (está no `.gitignore`).

1. Na pasta `Dev`, copie o arquivo `banco.properties.example`.
2. Renomeie a cópia para `banco.properties`.
   > No Windows, ative **Exibir → Extensões de nomes de arquivos** para não criar `banco.properties.example.properties` ou similar.
3. Abra no Bloco de Notas e ajuste a senha:

```properties
db.url=jdbc:postgresql://localhost:5432/SNCTech_DB
db.usuario=postgres
db.senha=SUA_SENHA_DO_POSTGRES
modo=banco
```

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
Tests run: 111, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

> Se aparecer `Skipped` maior que 0, os testes de integração com o banco foram pulados: confira o `banco.properties` e se as migrações foram rodadas.

### 5.3 Rodar o programa

1. No painel **Maven**: **snctech → Plugins → javafx → `javafx:run`** (dois cliques).
2. A tela de login abre. Clique em **Entrar**.

> **Não** rode o `App.java` pelo ▶ verde do editor: isso dá o erro *"JavaFX runtime components are missing"*. Use sempre o `javafx:run`.

---

## 6. Roteiro de testes

Usa os dados de exemplo (seeds). Cada passo tem a **ação** e o **resultado esperado**.

### Parte 1 — Antes de o professor finalizar a correção

| # | Ação | Resultado esperado |
|---|---|---|
| 1 | Painel do professor → **Notas e Médias** | Turma **"Turma Correção (exemplo)"** selecionada. Colunas "Prova de Exemplo - Correção / peso 1" e "Prova 2 - Média (exemplo) / peso 2". Ana e Bruno com **"Aguardando Correção do Professor"** nas duas provas e média **"–"**. Carla com **"0,00 (não realizada)"** nas duas provas e média **0,00 • Reprovado** (ela não fez nenhuma prova). Mensagem listando as pendências e botão **Liberar médias desativado**. |
| 2 | Voltar → **Provas Aplicadas** → **Visualização do aluno** → e-mail `ana@aluno.test.com` → **Ver minhas notas** | As duas provas com **"Aguardando Correção do Professor"**. Média final: *"A média final ainda não foi liberada pelo professor."* |

### Parte 2 — Correção e finalização pelo professor

| # | Ação | Resultado esperado |
|---|---|---|
| 3 | Provas Aplicadas → clicar no cartão **Prova de Exemplo - Correção** → **Corrigir prova** | Ana e Bruno com selo **"Pendente"** (1 discursiva pendente) e botão **Corrigir**. Carla com **"Não respondeu"**. |
| 4 | Clicar em **Corrigir** na linha da **Ana** | Selo **"Parcial"** e botão **Finalizar correção desativado**. |
| 5 | Na discursiva da Ana, digitar **2** → **Alterar nota** | Selo **"Falta finalizar"** (azul), nota **8,67** e botão **Finalizar correção ativo**. |
| 6 | Clicar em **Finalizar correção** | Selo **"Finalizada"** (verde). |
| 7 | Visualização do aluno com `ana@aluno.test.com` | Prova de Exemplo com nota **8,67**. Prova 2 ainda **"Aguardando Correção do Professor"**. |
| 8 | **Voltar à lista de alunos** (a Ana aparece **"Finalizada"**, botão **Revisar**) → **Corrigir** na linha do **Bruno** → nota **2** na discursiva → **Finalizar correção** | Nota do Bruno **4,50** e selo **"Finalizada"**. |
| 9 | Provas Aplicadas → cartão **Prova 2 - Média (exemplo)** → **Corrigir prova** → **Finalizar** na linha da **Ana** → **Finalizar correção**; depois escolher **Bruno** no seletor do alto → **Finalizar correção** | A prova já veio corrigida pelo sistema (só objetivas): na lista, os dois aparecem **"Falta finalizar"**. Ana **10,00** e Bruno **5,00**, ambos **"Finalizada"** no fim. |

### Parte 3 — Média final e liberação

| # | Ação | Resultado esperado |
|---|---|---|
| 10 | Painel → **Notas e Médias** | Tabela abaixo e botão **Liberar médias ativo**. |
| 11 | Visualização do aluno com a Ana | As duas notas aparecem, mas a Média final diz **"ainda não foi liberada"**. |
| 12 | Notas e Médias → **Liberar médias** → **OK** | Coluna "Para o aluno" = **"Liberada"**, mensagem *"Médias liberadas..."* e botão desativado. |
| 13 | Visualização do aluno com a Ana (e depois com `bruno@aluno.test.com`) | Ana: **Média final 9,56 • Aprovado**. Bruno: **4,83 • Exame final**. |

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
| 15 | Painel → **Notas e Médias** | Média da Ana **9,06**, continua **Liberada**. Na Visualização do aluno, a Ana vê **9,06**. |

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

1. No pgAdmin, **feche todas as abas do Query Tool**.
2. Botão direito em **`SNCTech_DB` → Delete (Force)** e confirme.
3. Crie o banco de novo ([seção 3.1](#31-criar-o-banco)).
4. Rode os 6 arquivos na ordem da [seção 3.3](#33-ordem-dos-arquivos).

---

## 8. Problemas comuns

| Problema | Causa | Solução |
|---|---|---|
| pgAdmin: *syntax error at or near "\\"* no backup | Versão antiga do `snctech_backup.sql` (com `\restrict`) | Use o `snctech_backup.sql` desta branch, que é compatível com o pgAdmin. |
| *relation "avaliacoes" does not exist* ao rodar uma migração | Script rodado na mesma aba do backup | Abra **uma aba nova** do Query Tool para cada arquivo, ou rode `SET search_path TO public;` antes. |
| *database "SNCTech_DB" is being accessed by other users* ao apagar | Abas do Query Tool abertas | Feche as abas ou use **Delete (Force)**. |
| Programa: *"Falta a tabela medias_finais..."*, *"O banco não está pronto para a Correção"* ou, na tela do aluno, *"column correcao_finalizada does not exist"* | Migração 03 não foi rodada | Rode `migracao_03_notas_e_medias.sql`. |
| Programa: erro de senha ou conexão | Senha errada no `banco.properties` | Confira o `db.senha` (seção 4). |
| *JavaFX runtime components are missing* | Programa rodado pelo ▶ do editor | Rode pelo Maven: **Plugins → javafx → javafx:run**. |
| *UnsupportedClassVersionError* | O Maven está usando um Java antigo | Configure o JDK 25 no projeto e no `JAVA_HOME`. |
| Arquivo virou `nome.sql.sql` | O Windows escondia a extensão ao renomear | Ative **Exibir → Extensões de nomes de arquivos** e renomeie. |
| Notas e Médias diz *"Você ainda não tem turmas cadastradas"* | Seeds não foram rodados | Rode `seed_02` e `seed_03`. |
| A Ana não aparece em Notas e Médias com as médias esperadas | Nota diferente de **2** nas discursivas | Use 2 nos passos 5 e 8, ou recomece do zero (seção 7). |
