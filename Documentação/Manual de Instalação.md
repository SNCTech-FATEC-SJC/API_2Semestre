# Manual de Instalação — SNCTECH — Avalia +

Este manual é para quem vai instalar, configurar e manter o sistema. Para usar o sistema, consulte o **Manual do Usuário**.

---

## 1. Requisitos

| Item | Versão | Observação |
|---|---|---|
| Java JDK | **25** | Obrigatório. Versões anteriores não compilam o projeto. |
| Apache Maven | versão recente | Gerencia as dependências e executa o programa. |
| PostgreSQL | versão recente | Servidor do banco de dados. Driver usado: 42.7.3. |
| IDE de Java de sua preferência | — | Ex.: IntelliJ IDEA, VS Code (com extensões Java), Eclipse ou NetBeans. |

---

## 2. Obter o projeto

O projeto é entregue como uma pasta (ou um arquivo `.zip`). Depois de extrair, a estrutura principal é:

```
API_2Semestre/
├── Dev/                  ← código-fonte, pom.xml, testes e scripts do banco
│   ├── pom.xml
│   ├── java/ e test/
│   ├── resources/
│   ├── Avisos.e.Banco/DB_SQL/   ← scripts SQL (executados pelo próprio programa)
│   └── banco.properties.example
└── Documentação/         ← backlog, critérios e documentos das sprints
```

> Todos os comandos deste manual são executados **dentro da pasta `Dev`**, onde está o `pom.xml`.

---

## 3. Instalar o Java JDK 25

1. Instale o **JDK 25** (qualquer distribuição, como Temurin, Oracle ou Microsoft).
2. Confira a instalação:

```bash
java -version
javac -version
```

Ambos devem mostrar a versão 25.

3. Se o Maven usar outra versão, configure a variável `JAVA_HOME` para apontar para a pasta do JDK 25.

---

## 4. Instalar o Maven

1. Instale o **Apache Maven** (versão recente).
2. Confira a instalação:

```bash
mvn -v
```

O Maven deve mostrar a versão e apontar para o Java 25 como runtime.

---

## 5. Instalar e configurar o PostgreSQL

1. Instale o **PostgreSQL** e anote a senha definida para o usuário `postgres`.
2. Confirme que o servidor está em execução e escutando na porta **5432** (padrão).
3. (Opcional) Abra o **pgAdmin** para conferir o servidor.

### 5.1 Criar o banco

Crie manualmente o banco `SNCTech_DB`:

```sql
CREATE DATABASE "SNCTech_DB" TEMPLATE template0 ENCODING 'UTF8';
```

Se o usuário configurado tiver permissão para criar bancos, o próprio programa cria o banco na primeira execução. Nesse caso, este passo é opcional.

> Não é preciso executar os scripts SQL manualmente. O programa aplica a estrutura e os dados de exemplo ao abrir (seção 8).

---

## 6. Configurar a conexão com o banco

A configuração pode ser feita de três formas, por ordem de prioridade:

1. **Propriedades do Java** (`-Dsnctech.db.url=...`)
2. **Variáveis de ambiente** (`SNCTECH_DB_URL`, `SNCTECH_DB_USUARIO`, `SNCTECH_DB_SENHA`, `SNCTECH_MODO`)
3. **Arquivo `banco.properties`**, dentro da pasta `Dev`

### 6.1 Arquivo `banco.properties`

1. Na pasta `Dev`, renomear `banco.properties.example` para `banco.properties`.
2. Ajuste os valores:

```properties
db.url=jdbc:postgresql://localhost:5432/SNCTech_DB
db.usuario=postgres
db.senha=sua_senha
modo=banco
```

Os valores padrão são:

| Chave | Padrão | Descrição |
|---|---|---|
| `db.url` | `jdbc:postgresql://localhost:5432/SNCTech_DB` | Endereço do banco |
| `db.usuario` | `postgres` | Usuário do PostgreSQL |
| `db.senha` | (vazio) | Senha do usuário |
| `modo` | `banco` | `banco` grava no PostgreSQL; `memoria` não grava nada |

> Se o arquivo não existir, o programa pede a senha do PostgreSQL na primeira abertura e cria o `banco.properties` sozinho.

> **Segurança:** o `banco.properties` contém a senha do banco. Ele já está no `.gitignore` e **não deve ser enviado ao repositório nem compartilhado**.

### 6.2 Modo demonstração (sem banco)

Para testar o sistema sem PostgreSQL, use `modo=memoria`. Nesse modo, nada é salvo: ao fechar o programa, os dados se perdem. Útil para demonstrações rápidas.

---

## 7. Executar o sistema

### 7.1 Pelo terminal

Na pasta `Dev`:

```bash
mvn clean compile
mvn javafx:run
```

### 7.2 Pelo IntelliJ IDEA

1. Abra a pasta `Dev` no IntelliJ.
2. Aguarde o Maven baixar as dependências.
3. Execute a classe principal **`br.snctech.App`**.

### 7.3 Primeira execução

Na primeira execução, o programa:

1. conecta ao PostgreSQL (ou pede a senha, se não houver `banco.properties`);
2. cria o banco, se necessário e se o usuário tiver permissão;
3. aplica a estrutura base (`snctech_backup.sql`);
4. aplica as migrações e os dados de exemplo, na ordem numérica;
5. registra cada script aplicado, para não repetir na próxima abertura;
6. mostra a tela de login.

Os scripts são executados nesta ordem: `migracao_01`, `02`, `03`, `04`, `05`, depois os `seed_`.

> **Dados de exemplo:** os arquivos `seed_02_correcao_exemplo.sql` e `seed_05_medias_exemplo.sql` criam dados para demonstração. Antes de usar com dados reais, confirme com a equipe se eles devem permanecer no banco.

---

## 8. Contas iniciais e segurança

O script base cria contas iniciais para teste, com perfis de professor, aluno e administrador. **Essas contas usam senhas fracas e conhecidas.** Antes de usar o sistema com dados reais:

1. Entre com a conta de administrador e troque a senha de todas as contas de teste, ou
2. Remova as contas de teste diretamente no banco.

> **Observação técnica:** a coluna `senha_hash` da tabela `usuarios` guarda as senhas sem criptografia nessas contas de teste. Para uso real, a equipe deve passar a gravar as senhas com hash antes de cadastrar usuários.

Outras recomendações:

- Use uma senha forte para o usuário `postgres`.
- Limite o acesso ao PostgreSQL à rede necessária.
- Não compartilhe o `banco.properties`.

---

## 9. Administração do banco (opcional)

Além da execução automática, há um arquivo separado com ajustes e consultas de administração: **`pendencias_banco.sql`** (entregue junto com a documentação, fora da pasta de scripts executados pelo programa). Ele contém:

- verificações do estado atual do banco;
- restrições e índices (peso positivo, índice por turma, índice único de alternativas);
- rotina para encerrar tentativas vencidas;
- consultas de diagnóstico (somente leitura).

> **Atenção:**
> - Faça **backup** antes de executar qualquer comando de escrita.
> - Teste em uma **cópia do banco**, nunca direto no banco de uso real.
> - Execute as consultas de diagnóstico (seção 5) **antes** dos comandos de criação de índice único. O índice falha se já houver alternativas repetidas na mesma questão.
> - As consultas de encerramento e de respostas do aluno ainda não foram validadas no PostgreSQL. Revise-as antes de usar.

---

## 10. Problemas comuns na instalação

| Sintoma | Causa provável | Solução |
|---|---|---|
| `mvn` não é reconhecido | Maven não está no `PATH` | Instale o Maven e reabra o terminal |
| Erro de versão do Java / `release 25` | JDK diferente do 25 | Instale o JDK 25 e ajuste `JAVA_HOME` |
| O programa pede a senha em toda abertura | `banco.properties` não foi criado ou não está em `Dev` | Crie o arquivo na pasta `Dev` (seção 6.1) |
| `senha não funcionou` | Senha incorreta | Digite novamente; confirme a senha do usuário `postgres` |
| `Connection refused` | PostgreSQL parado ou porta errada | Inicie o serviço e confira a porta 5432 |
| `database "SNCTech_DB" does not exist` | Banco não criado e usuário sem permissão | Crie o banco (seção 5.1) |
| Erro ao iniciar o JavaFX | Execução fora da pasta `Dev` | Rode `mvn javafx:run` dentro de `Dev` |
| Testes de integração falham | Banco de teste não configurado | Configure um banco de teste (seção 9.2) |
| `modo=memoria` e os dados somem | Comportamento esperado do modo demonstração | Use `modo=banco` para gravar |

---

