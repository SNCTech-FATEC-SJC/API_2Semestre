# 📋 Backlog da Sprint 2

**Equipe:** SNCTECH

\---

## Itens do Backlog

|ID|Épico|User Story|Critério de Aceitação|Prioridade|Status|
|-|-|-|-|-|-|
|PB-01|Gerenciamento de Avaliação|Como professor, desejo gerenciar as informações das avaliações para reutilizar avaliações já aplicadas e criar novas avaliações|O professor consegue visualizar e editar as avaliações já cadastradas, sem alterar as avaliações antigas já respondidas pelos alunos|🔴 Alta|Status: ❌|
|PB-02|Gerenciamento de questões|Como professor, desejo gerenciar questões para centralizá-las em um único lugar|O professor consegue cadastrar e editar questões na ferramenta, sendo obrigatórios o enunciado e a definição da resposta correta|🔴 Alta|Status: ❌|
|PB-03|Correção Manual|Como professor, desejo a correção manual para corrigir questões discursivas e verificar a nota final.|O professor acessa cada avaliação e consegue corrigi-la|🔴 Alta|Status: ❌|
|PB-04|Embaralhamento de Respostas|Como professor, desejo que as respostas de questões de múltipla escolha e associação sejam embaralhadas para reduzir a possibilidade de compartilhamento da ordem das respostas entre os alunos.|Ao abrir a prova, as alternativas são aleatorizadas|🟡 Média|Status: ❌|
|PB-05|Disponibilização de Avaliação|Como professor, desejo disponibilizar uma avaliação para uma ou mais turmas para que os alunos possam ter acesso a ela|O professor libera o acesso à data da avaliação e os alunos conseguem acessar a página da avaliação, porém sem conseguir abri-la|🟡 Média|Status: ❌|
|PB-06|Realização das avaliações|Como professor, desejo que os alunos realizem a prova para centralizar todas as avaliações.|Após o professor definir o prazo da avaliação e esse prazo chegar, os alunos conseguem responder à avaliação|🟡 Média|Status: ❌|
|PB-07|Realização das avaliações|Como aluno, desejo realizar as avaliações para registrar minhas respostas e ser avaliado|Após a liberação do professor, o aluno acessa a avaliação e consegue respondê-la.|🟡 Média|Status: ❌|
|PB-08|Correção Automática|Como professor, desejo uma correção automática para as questões de múltipla escolha e assimilação para facilitar a correção das avaliações.|As questões respondidas estão corretas com base nas respostas registradas pelo professor.|🟡 Média|Status: ❌|
|PB-11|Visualização Avaliação|Como aluno, desejo visualizar minhas avaliações já realizadas e as futuras para ter controle das minhas notas e planos de estudo|Após responder à prova, o aluno pode acessá-la e ver as datas de avaliações futuras|🟡 Média|Status: ❌|
|PB-12|Notas|Como professor, desejo que o cálculo das notas seja gerado automaticamente para facilitar a correção.|Após a correção das questões, a nota é armazenada no sistema.|🟡 Média|Status: ❌|



\---

## 

## ✅ Definition of Ready (DoR)

## 

## DoR Geral

* Está claro o que a funcionalidade deve permitir ao usuário
* Está claro o que faz e o que não faz parte da US
* Não existem decisões pendentes
* A US não depende de outra US não concluída
* Os critérios de aceitação estão claros
* O esboço da tela está feito e aprovado
* O esforço da US está estimado

> De forma geral: uma User Story está pronta para desenvolvimento quando existirem informações suficientes para a equipe iniciar o desenvolvimento sem precisar tomar decisões fundamentais sobre ela.



### DoR Específico

|ID|Critério|
|-|-|
|PB-01|Está definido como o professor seleciona as questões que farão parte da avaliação|
|PB-01|Está definido o que pode ser alterado na avaliação já existente|
|PB-05|Está definido como será feita a validação do período da avaliação|
|PB-05|Está definido como a aplicação variará entre não iniciada, disponível e encerrada|
|PB-06|É possível diferenciar Não Iniciou / Em andamento / Concluiu|
|PB-11|Conteúdo exibido para avaliação concluída definido|

\---

## 

## 🏁 Definition of Done (DoD)

### 

### DoD Geral

* Implementação concluída baseada em todos os requisitos da US
* Critério de aceitação atendido e verificável
* Integração sem interferência com funcionalidades anteriores
* Permissões e restrições de acesso são respeitadas
* Interface funcional e validada
* Testes executados por outro integrante da equipe
* Nenhum defeito conhecido que impeça o uso da funcionalidade
* Código revisado e validado pela equipe
* Documentação atualizada
* Commits claros que seguem o padrão
* Passou por pull request para a branch principal aprovado pelo P.O.

> Uma User Story está pronta quando foi implementada, validada e integrada ao sistema de acordo com seu escopo, regras de negócio e critérios de aceitação, sem defeitos conhecidos que comprometam seu uso.

### 

### DoD Específico

|ID|Critério|
|-|-|
|PB-01|Versões anteriores devem permanecer disponíveis para consulta|
|PB-03|O professor pode alterar uma correção já realizada|
|PB-04|As alternativas devem aparecer em ordens diferentes para diferentes alunos|
|PB-04|A identificação da resposta correta se mantém vinculada ao conteúdo, e não à sua posição visual|
|PB-04|O sistema deve validar a alternativa correta após o embaralhamento|
|PB-04|Não é preciso garantir uma ordem diferente para todos os alunos quando não houver possibilidades suficientes|
|PB-05|O acesso é bloqueado antes do início|
|PB-05|Ao atingir o horário de término, os alunos não conseguem alterar/enviar as respostas|
|PB-05|Respostas já registradas devem ser preservadas|
|PB-05|Após o início da avaliação, nenhuma alteração nela deve ser permitida|
|PB-05|O encerramento ocorre automaticamente ao atingir o horário definido|
|PB-07|Respostas devem ser salvas durante a realização|
|PB-07|O aluno que não iniciar até o encerramento deve receber nota 0|
|PB-07|O aluno que iniciar, mas não concluir, deve ter suas respostas já registradas preservadas|
|PB-07|Questões não respondidas pelo aluno devem ser consideradas incorretas, recebendo 0 ponto|
|PB-07|O aluno não deve conseguir alterar respostas após o encerramento|
|PB-08|Questões objetivas incorretas recebem 0 inicialmente|
|PB-08|Uma pontuação pode ser alterada pelo professor por correção manual|
|PB-08|Questões de associação recebem nota parcial proporcional à quantidade de associações corretas|
|PB-08|A correção respeita a alternativa correta independentemente da posição|

\---

## Legenda

|Símbolo|Prioridade|
|-|-|
|🔴|Alta|
|🟡|Média|
|🟢|Baixa|

|Status|Significado|
|-|-|
|✅ Concluído|Item entregue e validado|
|❌ Não finalizado|Item ainda não concluído|



