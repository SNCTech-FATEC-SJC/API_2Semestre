# Manual do Usuário — SNCTECH — Avalia +

## 1. Sobre o sistema

O **Avalia +** é um sistema para criar, aplicar e corrigir avaliações escolares. O professor cadastra questões, monta as provas, acompanha as respostas, faz a correção e libera as médias. O aluno responde às provas liberadas para ele e consulta suas respostas, correções e notas.

O sistema tem três perfis de acesso:

- **Professor:** banco de questões, avaliações, turmas, alunos, correção e notas.
- **Aluno:** avaliações disponíveis, respostas, consulta de correção e notas.
- **Administrador:** gerenciamento de turmas e alunos, além das demais áreas de gestão.

Se você ainda não tem uma conta, peça ao administrador do sistema.

---

## 2. Entrar no sistema

A tela de entrada pede **E-mail** e **Senha**. Entre com a conta que foi cadastrada para você. O perfil da conta define o menu que aparece depois do login.

Para sair, use **Sair da conta** no menu lateral. Para fechar o programa, use **Fechar aplicação**.

---

## 3. Como utilizar — Professor

Ao entrar, o painel do professor mostra o menu lateral com:

- **Banco de Questões**
- **Provas Aplicadas**
- **Gerenciamento de Turmas**
- **Gerenciamento de Alunos**
- **Notas e Médias**
- **Sair da conta** e **Fechar aplicação**

### 3.1 Cadastrar questões

1. Acesse **Banco de Questões**.
2. Clique em **Adicionar Questão**.
3. Escolha o **Tipo de questão**:
   - **Múltipla Escolha:** digite as alternativas e marque o círculo da alternativa correta.
   - **Verdadeiro/Falso:** marque a resposta correta.
   - **Associação:** monte os **Pares correspondentes** com **Adicionar par**. Use **Remover par selecionado** para apagar um par.
   - **Dissertativa:** informe a **Resposta esperada**, usada como referência na correção.
4. Digite o **Enunciado**.
5. Se quiser, anexe uma **Imagem do enunciado** (opcional).
6. Salve a questão.

### 3.2 Gerenciar turmas

Em **Gerenciamento de Turmas**:

- **Nova turma** cria uma turma.
- **Editar** altera os dados da turma selecionada.
- **Ver membros** mostra os alunos matriculados.
- **Ver avaliação** mostra as avaliações ligadas à turma.
- **Concluir turma** encerra a turma. Só o professor responsável pela turma ou o administrador pode concluí-la ou reativá-la.

### 3.3 Gerenciar alunos

Em **Gerenciamento de Alunos**:

- **Novo aluno** cadastra um aluno. Nome, e-mail e **senha são obrigatórios**.
- **Editar** altera os dados. Deixe a senha em branco para mantê-la.
- **Remover** exclui o cadastro selecionado.
- **Gerenciar turmas** define em quais turmas o aluno está matriculado.

### 3.4 Criar avaliação

1. Acesse **Provas Aplicadas** e clique em **Nova Prova**.
2. Preencha:
   - **Nome da avaliação**;
   - **Turmas que poderão realizar a prova** (pode marcar mais de uma);
   - **Alunos que poderão realizar a prova** (use **Selecionar todos os alunos** para marcar todos);
   - **Data da avaliação**, **Hora de início** (HH:mm) e **Hora de término** (HH:mm);
   - **Prazo de entrega**;
   - **Peso da avaliação** (ex.: `1,0`). O peso é usado no cálculo da média final.
3. Marque **pelo menos uma questão** em **Selecione pelo menos uma questão**.
4. Clique em **Salvar avaliação**.

> A prova só pode ser aberta pelo aluno dentro do período definido. Depois do prazo, ela aparece como encerrada para o aluno.

### 3.5 Consultar, editar e corrigir avaliações

1. Acesse **Provas Aplicadas** (tela **Avaliações cadastradas**).
2. Selecione a avaliação. A tela de detalhes mostra as **Questões desta avaliação**.
3. Use os botões disponíveis:
   - **Editar avaliação**: altera os dados da prova.
   - **Corrigir prova**: abre a lista de alunos para correção.
   - **Voltar**: retorna à lista.

> **Editar uma avaliação já respondida:** o sistema cria uma **nova versão** da prova. As respostas dos alunos continuam na versão original. A nova versão **não fica liberada**: é preciso liberá-la novamente.

### 3.6 Corrigir respostas

1. Em **Corrigir prova**, a tela **Correção da prova** mostra os alunos e o total já corrigido.
2. As questões de **múltipla escolha**, **verdadeiro/falso** e **associação** são corrigidas automaticamente.
3. As questões **dissertativas** precisam de correção manual. Abra a **Correção manual** do aluno, avalie cada resposta e salve.
4. Quando a correção de um aluno estiver completa, ela aparece como concluída na lista.

### 3.7 Notas e médias

1. Acesse **Notas e Médias** e escolha a turma.
2. Clique em **Atualizar** para recalcular o quadro com as correções mais recentes.
3. Confira as médias de cada aluno.
4. Clique em **Liberar médias** para que os alunos vejam a média final e a situação. Depois de liberadas, se uma nota for alterada na correção manual, a média é recalculada automaticamente.

---

## 4. Como utilizar — Aluno

Ao entrar, o painel do aluno mostra o menu lateral com **Minhas Turmas**, **Minhas Avaliações** e **Minhas Notas**.

### 4.1 Consultar avaliações

Em **Minhas Avaliações** (tela **Avaliações disponibilizadas**), as provas aparecem em seções:

- **Avaliações disponíveis:** provas que você pode responder agora, com o botão **Responder avaliação**.
- **Avaliações futuras:** provas que ainda não começaram. Não é possível abrir nem ver as questões.
- **Respondidas / em andamento:** provas já iniciadas. Se a prova estiver em andamento, aparece o botão **Retomar avaliação**.
- **Encerradas:** provas com prazo vencido.

Nas seções **Respondidas / em andamento** e **Encerradas**, o botão **Consultar respostas e correção** mostra suas respostas, a correção de cada questão e a situação.

### 4.2 Responder avaliações

1. Em **Avaliações disponíveis**, clique em **Responder avaliação**.
2. A tela **Responder avaliação** mostra as questões. Leia cada enunciado e marque ou escreva a resposta.
3. Se houver cronômetro na tela, acompanhe o tempo restante.
4. Para sair sem entregar, clique em **Sair sem entregar**. As respostas já marcadas ficam salvas.
5. Quando terminar, clique em **Entregar avaliação**.

> Cada resposta é salva durante a prova. Depois da entrega, não é possível responder a prova de novo.

### 4.3 Retomar uma avaliação

Se você saiu de uma prova sem entregar, ela aparece em **Respondidas / em andamento** com o botão **Retomar avaliação**. Você continua de onde parou, desde que ainda esteja dentro do período da prova.

### 4.4 Consultar notas

Em **Minhas Notas** (tela **Notas e correções**), você vê as notas de cada avaliação corrigida.

A **média final** aparece somente depois que o professor libera as médias da turma. Enquanto isso, a tela mostra que a média ainda não foi liberada.

---

## 5. Problemas comuns

- **Não consigo entrar:** confira o e-mail e a senha. Se a conta não funcionar, fale com o administrador.
- **Não vejo uma avaliação:** confira se ela está liberada para você, se você está em uma das turmas ou na lista de alunos selecionados, e se o período da prova já começou ou ainda não terminou. Avaliações sem data e horários completos não aparecem.
- **Não vejo minha nota:** o professor precisa clicar em **Liberar médias** para a turma. Também é preciso que as correções manuais tenham sido feitas.
- **A prova não abre:** verifique se o horário atual está dentro do período da prova. Se a prova já foi entregue, ela não pode ser respondida de novo.
- **Alterações não aparecem na tela:** use **Atualizar** (em Notas e Médias) ou feche e abra a tela novamente. Se persistir, reinicie o programa.
- **O programa não abre ou mostra erro de conexão com o banco:** isso é resolvido por quem instalou o sistema. Consulte o **Manual de Instalação**.

---

## 6. Glossário

- **Avaliação:** prova montada pelo professor com questões, turmas, alunos, datas e peso.
- **Tentativa:** a resposta de um aluno a uma avaliação.
- **Versão:** cópia de uma avaliação criada quando ela é editada depois de respondida.
- **Média final:** média ponderada das avaliações do aluno, calculada pelos pesos. Só aparece depois da liberação pelo professor.
