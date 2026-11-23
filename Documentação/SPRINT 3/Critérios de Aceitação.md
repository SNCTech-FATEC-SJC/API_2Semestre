# ✅ Critérios de Aceitação da Sprint 3

**Equipe:** SNCTECH

\---



## Detalhamento por Item do Backlog



### PB-04 · Embaralhamento de Respostas

**Critério de aceitação:** Ao abrir a prova, as alternativas são aleatorizadas

**Detalhamento:**

* Ao abrir a prova, as alternativas são aleatorizadas





### PB-13 · Notas

**Critério de aceitação:** Após a correção do sistema, o aluno consegue acessar sua nota

**Detalhamento:**

* Considerando que a avaliação já foi corrigida, o aluno consegue visualizar a nota obtida nela
* O aluno só pode visualizar as suas próprias notas
* Considerando que a avaliação ainda não foi corrigida, a nota não é exibida ao aluno





### PB-14 · Notas

**Critério de aceitação:** Após a correção do sistema, o professor consegue visualizar a nota dos alunos

**Detalhamento:**

* Considerando que o aluno respondeu a avaliação e ela foi corrigida, o professor consegue visualizar a nota dele
* O professor pode visualizar as notas dos alunos das turmas em que leciona





### PB-15 · Média Final

**Critério de aceitação:** Com a liberação da média final pelo sistema, o aluno consegue visualizá-la

**Detalhamento:**

* Considerando que todas as avaliações previstas já foram encerradas e corrigidas, o aluno consegue visualizar a sua média final
* Considerando que ainda existem notas pendentes, a média final não é exibida
* Considerando que a média final foi calculada, o aluno visualiza sua situação: Aprovado (média maior ou igual a 6), Exame Final – EXF (média maior ou igual a 4 e menor que 6) ou Reprovado (média menor que 4)





### PB-16 · Média Final

**Critério de aceitação:** A média final é definida corretamente com base nas notas e nos pesos das provas

**Detalhamento:**

* Considerando que todas as notas do aluno estão disponíveis, o sistema soma as notas de acordo com o peso de cada avaliação e define a média final
* Considerando que uma nota foi alterada por correção manual, a média final é recalculada automaticamente





### PB-17 · Média Final

**Critério de aceitação:** Com a liberação da média final pelo sistema, o professor consegue visualizá-la

**Detalhamento:**

* Considerando que a média final dos alunos foi calculada, o professor consegue visualizá-la, junto com a situação de cada aluno
* O professor pode visualizar as médias finais dos alunos das turmas em que leciona





### PB-18 · Prazo Das Avaliações

**Critério de aceitação:** O professor, após criar uma avaliação, pode definir um prazo para uma turma respondê-la

**Detalhamento:**

* Considerando que o professor já criou a avaliação, ele deve definir um prazo (data e hora de início e de fim) para a turma
* Considerando que o prazo ainda não começou ou já terminou, o aluno não consegue iniciar a avaliação
* Considerando que o aluno não respondeu a avaliação dentro do prazo, a nota será 0





### PB-19 · Autenticação

**Critério de aceitação:** O usuário consegue acessar o sistema com suas credenciais, diferenciando cada usuário e se ele é professor ou aluno

**Detalhamento:**

* Considerando que o usuário informou credenciais válidas, ele consegue acessar o sistema
* Considerando que o usuário informou credenciais inválidas, o acesso é negado e uma mensagem é exibida
* Considerando que o usuário acessou o sistema, ele visualiza apenas as funcionalidades do seu perfil (professor ou aluno)
