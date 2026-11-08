# Manual do Usuário - SNCTECH



## 1\. Sobre o sistema

O **SNCTECH** é um sistema para criar, aplicar e corrigir avaliações
escolares, além de permitir que os alunos respondam às provas e
consultem suas notas.

O sistema possui dois perfis: **Professor** e **Aluno**.





## 2\. Requisitos para utilizar o sistema

Para executar o SNCTECH, é necessário ter instalado:

* Java JDK 25;
* Maven;
* PostgreSQL;
* Projeto SNCTECH no computador.





## 3\. Preparação inicial

Antes de iniciar o sistema:

1. Abra o PostgreSQL e crie o banco de dados `SNCTech\_DB`.
2. No pgAdmin, abra o **Query Tool** desse banco.
3. Execute o script `Dev/DB\_SQL/snctech\_backup.sql`, fornecido no
projeto.
4. Na pasta `Dev`, copie o arquivo `banco.properties.example` e
renomeie a cópia para `banco.properties`.
5. Abra o arquivo e configure o endereço do banco, o usuário e a senha
do PostgreSQL.

Exemplo:

``` properties
db.url=jdbc:postgresql://localhost:5432/SNCTech\\\\\\\_DB
db.usuario=postgres
db.senha=sua\\\\\\\_senha
modo=banco
```

## 4\. Iniciar o sistema

1. Abra a pasta `Dev` no IntelliJ IDEA.
2. Aguarde o Maven carregar as dependências.
3. Abra o terminal na pasta onde está o arquivo `pom.xml`.
4. Execute o comando:

``` bash
mvn javafx:run
```





## 5\. Acesso ao sistema

* **Professor:** cadastra questões, cria avaliações, gerencia turmas e
acompanha correções.
* **Aluno:** consulta avaliações disponíveis, responde às provas e
consulta notas.





## 6\. Como utilizar - Professor

### Cadastrar questões

1. Acesse **Banco de Questões**.
2. Abra a opção de cadastro.
3. Escolha o tipo de questão.
4. Preencha o enunciado e as alternativas, quando necessário.
5. Informe a resposta correta, quando solicitado.
6. Salve a questão.



### Criar avaliações

1. Acesse **Provas Aplicadas**.
2. Clique em **Nova Prova**.
3. Informe o nome da avaliação e a turma.
4. Defina a data, os horários e o prazo de entrega.
5. Informe a duração da prova em minutos. O valor `0` representa tempo
ilimitado.
6. Selecione pelo menos uma questão.
7. Clique em **Salvar avaliação**.



### Consultar e corrigir avaliações

1. Acesse **Avaliações cadastradas**.
2. Selecione a avaliação desejada.
3. Utilize as opções disponíveis para visualizar, editar ou corrigir a
prova.
4. Revise as respostas dos alunos e faça a correção manual das questões
discursivas, quando necessário.





## 7\. Como utilizar - Aluno

### Consultar avaliações

1. Acesse a área **Visualização do aluno**.
2. Informe o e-mail cadastrado, quando solicitado.
3. Consulte a lista de avaliações disponíveis.



### Responder avaliações

1. Selecione a avaliação desejada.
2. Leia os enunciados e responda às questões.
3. Observe o cronômetro, caso exista limite de tempo.
4. Ao terminar, clique em **Entregar avaliação**.



### Consultar notas

Na área do aluno, utilize a opção **Ver minhas notas**, quando
disponível, para consultar os resultados.





## 8\. Problemas comuns

* **O sistema não inicia:** confira se o Java 25 está instalado e se o
comando foi executado na pasta correta.
* **Erro de conexão:** verifique se o PostgreSQL está iniciado e se os
dados do arquivo `banco.properties` estão corretos.
* **Alterações não aparecem:** reinicie o sistema e confira se os
dados foram salvos no banco correto.

