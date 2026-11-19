# 📋 Backlog da Sprint 3

**Equipe:** SNCTECH

\---

## Itens do Backlog

|ID|Épico|User Story|Critério de Aceitação|Prioridade|Status|
|-|-|-|-|-|-|
|PB-04|Embaralhamento de Respostas|Como professor, desejo que as respostas de questões de múltipla escolha e associação sejam embaralhadas para reduzir a possibilidade de compartilhamento da ordem das respostas entre os alunos.|Ao abrir a prova, as alternativas são aleatorizadas|🟡 Média|Status: ❌|
|PB-16|Média Final|Como professor, desejo que o cálculo da média seja feito de forma automática para facilitar o acesso a essa informação.|A média final é definida corretamente com base nas notas e nos pesos das provas|🟡 Média|Status: ❌|
|PB-17|Média Final|Como professor, desejo visualizar as médias finais dos alunos para ter controle de aprovação e desempenho.|Com a liberação da média final pelo sistema, o professor consegue visualizá-la|🟡 Média|Status: ❌|
|PB-18|Prazo Das Avaliações|Como professor, desejo definir um prazo para as avaliações a fim de evitar que alunos façam antes da hora ou depois do prazo.|O professor, após criar uma avaliação, pode definir um prazo para uma turma respondê-la|🟡 Média|Status: ❌|
|PB-13|Notas|Como aluno, desejo visualizar minhas notas para acompanhar meu desempenho.|Após a correção do sistema, o aluno consegue acessar sua nota|🟢 Baixa|Status: ❌|
|PB-14|Notas|Como professor, desejo visualizar as notas dos alunos para ter controle de aprovação e desempenho.|Após a correção do sistema, o professor consegue visualizar a nota dos alunos|🟢 Baixa|Status: ❌|
|PB-15|Média Final|Como aluno, desejo visualizar minha média final para ter conhecimento da minha aprovação ou reprovação|Com a liberação da média final pelo sistema, o aluno consegue visualizá-la|🟢 Baixa|Status: ❌|
|PB-19|Autenticação|Como usuário, desejo realizar login no sistema para acessar as funcionalidades correspondentes ao meu perfil.|O usuário consegue acessar o sistema com suas credenciais, diferenciando cada usuário e se ele é professor ou aluno|🟢 Baixa|Status: ❌|

\---

## ✅ Definition of Ready (DoR)

## DoR Geral

* Está claro o que a funcionalidade deve permitir ao usuário
* Está claro o que faz e o que não faz parte da US
* Não existem decisões pendentes
* A US não depende de outra US não concluída
* Os critérios de aceitação estão claros
* O esboço da tela está feito e aprovado
* O esforço da US está estimado



### DoR Específico

|ID|Critério|
|-|-|
|PB-15|Estão definidas as faixas de situação do aluno: Aprovado, Exame Final (EXF) e Reprovado|
|PB-16|Está definido como os pesos das avaliações são aplicados no cálculo da média|
|PB-18|Está definido como o prazo se relaciona com a disponibilização da avaliação para a turma|

\---

## 🏁 Definition of Done (DoD)

### DoD Geral

Uma User Story está Done quando foi implementada, validada e integrada ao sistema de acordo com seu escopo, regras de negócio e critérios de aceitação, sem defeitos conhecidos que comprometam seu uso.

* Implementação concluída com base em todos os requisitos da US.
* Critério de aceitação atendido e verificável.
* Fluxos principais e alternativos funcionando.
* Integração sem interferência com funcionalidades anteriores.
* Permissões e restrições de acesso são respeitadas.
* Interface funcional e validada.
* Testes executados por outro integrante da equipe.
* Nenhum defeito conhecido que impeça o uso da funcionalidade.
* Código revisado e validado pela equipe.
* Documentação atualizada quando necessário.

### DoD Específico por Épico (PBS)

|ID|Requisitos de Entrega e Conclusão (DoD)|
|-|-|
|**PB-04**|• As alternativas devem aparecer em ordem diferente para diferentes alunos.<br />• A identificação da resposta correta mantém-se ao conteúdo e não à sua posição visual.<br />• O sistema deve validar a alternativa correta após o embaralhamento.<br />• Não é preciso garantir uma ordem diferente para todos os alunos quando não houver possibilidades suficientes.|
|**PB-13**|• O aluno visualiza somente as suas próprias notas.|
|**PB-14**|• O professor visualiza somente as notas dos alunos das suas turmas.|
|**PB-15**|• A média só é liberada após todas as avaliações previstas estarem encerradas e corrigidas.<br />• A situação acadêmica é calculada a partir da média final.<br />• Regras de aprovação: - `Nota >= 6`: Aprovado - `4 <= Nota < 6`: Exame Fina - `Nota < 4`: Reprovado|
|**PB-16**|• A média considera as notas de cada avaliação com seus respectivos pesos.<br />• A média é recalculada automaticamente quando uma nota é alterada por correção manual.|
|**PB-17**|• O professor visualiza a média final e a situação de cada aluno.|
|**PB-18**|• O prazo é definido por turma.<br />• O aluno não consegue iniciar a avaliação fora do prazo.<br />• O aluno que não responder dentro do prazo recebe nota 0.|
|**PB-19**|• O acesso só é liberado com credenciais válidas.<br />• Cada perfil (professor ou aluno) acessa apenas as funcionalidades correspondentes.|

\---

## Legenda

|Símbolo|Prioridade|
|-|-|
|🟡|Média|
|🟢|Baixa|



