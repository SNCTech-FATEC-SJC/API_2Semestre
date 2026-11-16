# Correção das importações após a organização dos pacotes

Foram alinhados os diretórios e as declarações `package` dos arquivos Java:
- `banco`
- `avaliacoes`
- `correcao`
- `questoes`
- `turmas`
- `dashboard/controller`
- `util`

Também foram atualizadas as referências à classe `NavegacaoUtil`, e os arquivos de demonstração da correção foram isolados em `correcao/demo`, para não conflitar com o `App.java` principal.

Os arquivos gerados em `target/` foram removidos para evitar que classes compiladas antigas confundam a validação. O Maven não estava disponível neste ambiente, então a compilação completa não pôde ser executada aqui. Imports de classes internas (por exemplo, `Questao.ParColuna`) foram mantidos porque apontam para tipos aninhados, não para arquivos independentes.

Ao abrir no IntelliJ, execute `mvn clean javafx:run` a partir da pasta `Dev`. Se o seu projeto usa Java 25, confirme que o IntelliJ/Maven está configurado com esse JDK.
