# Organização do código — SNCTECH

A organização agrupa as classes por funcionalidade. A classe principal `App.java` permanece diretamente em `br.snctech`, conforme solicitado.

## Estrutura principal

```text
java/br/snctech/
├── App.java
├── autenticacao/
│   └── controller/
│       └── LoginController.java
├── banco/
│   ├── Conexao.java
│   ├── ErroBanco.java
│   ├── QuestaoDAO.java
│   ├── SessaoUsuario.java
│   └── UsuarioDAO.java
├── dashboard/
│   └── controller/
│       └── DashboardProfessorController.java
├── infraestrutura/
│   └── DadosAplicacao.java
├── util/
│   └── NavegacaoUtil.java
├── questoes/
│   ├── controller/
│   ├── model/
│   ├── repositorio/
│   └── service/
├── avaliacoes/
│   ├── controller/
│   ├── model/
│   ├── repositorio/
│   └── service/
├── correcao/
│   ├── controller/
│   ├── corretor/
│   ├── model/
│   ├── repositorio/
│   └── service/
├── turmas/
│   ├── controller/
│   └── repositorio/
└── testes/
    ├── TestarConexao.java
    ├── TesteAvaliacao.java
    └── TesteRealizacao.java
```

## Responsabilidade das pastas

- `autenticacao`: telas e controle de acesso/login.
- `dashboard`: tela inicial do professor e navegação para as funções.
- `infraestrutura`: objetos compartilhados pela aplicação, como repositórios usados durante a execução.
- `util`: utilitários compartilhados, como a navegação entre telas.
- `questoes`: cadastro e gerenciamento de questões.
- `avaliacoes`: criação, edição, visualização e realização de avaliações.
- `correcao`: correção automática e manual, seus modelos, serviços e repositórios.
- `turmas`: gerenciamento de turmas; a estrutura está preparada para receber as próximas classes da funcionalidade.
- `testes`: classes de teste executáveis por `main`, separadas do código das telas e funcionalidades.
- `banco`: conexão e componentes de acesso geral ao banco de dados.

Os arquivos FXML continuam em `resources/br/snctech`. As referências `fx:controller` foram atualizadas para os novos pacotes. O utilitário de navegação usa caminhos absolutos para continuar encontrando os FXML e o CSS após sua mudança de pacote.

> Observação: esta alteração reorganiza os arquivos e suas referências; ela não implementa as funcionalidades de gerenciamento de turmas que ainda serão desenvolvidas.


## Organização complementar (versão 3)

- `br.snctech.App`: permanece diretamente no pacote principal, conforme solicitado.
- `br.snctech.banco`: classes de conexão, acesso a usuários e questões e tratamento de erros do banco.
- `br.snctech.questoes.legado`: modelo antigo de questão mantido separado para evitar conflito com o modelo atual usado pelo módulo de questões.
- `br.snctech.correcao`: `DadosCorrecao` e demais classes da funcionalidade de correção.
- `br.snctech.correcao.demo`: `App` e `Launcher` antigos da demonstração isolada da correção, separados do ponto de entrada principal.
