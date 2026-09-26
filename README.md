
# SNCTECH - Gerenciamento e Aplicação de Avaliações Escolares
# Projeto API - 2º Semestre 2026
<p align="center">
  <img src="Documentos/logo_snctech.png" width="200"/>
</p>
<p align="center">
    <a href="#sobre"> Sobre</a> |
    <a href="#desafio"> Desafio</a> |
    <a href="#solução"> Solução</a> |
    <a href="#backlog"> Backlog do Produto</a> |
    <a href="#sprints"> Cronograma das Sprints</a> |
    <a href="#documentacao"> Documentação</a> |
    <a href="#tecnologias"> Tecnologias</a> |
    <a href="#equipe"> Equipe</a>
</p>

### 📌 Sobre o Projeto <a id="sobre"></a>

SNCTECH é um grupo composto por alunos do 2° semestre do curso de Banco de Dados da FATEC, e nesse semestre temos o objetivo de centralizar o ciclo completo de uma avaliação escolar: da criação das questões e da prova pelo professor, passando pela aplicação para os alunos, até a correção (manual e automática), o lançamento de notas e o cálculo da média final.

O sistema atende a dois perfis de usuário:
- **Professor**: cria e gerencia questões, avaliações, turmas e alunos; disponibiliza avaliações; corrige questões discursivas; acompanha notas e médias.
- **Aluno**: realiza avaliações disponibilizadas; visualiza avaliações já respondidas e futuras; acompanha suas notas e média final.

---

> Status do projeto: Em desenvolvimento

### 🎯 Desafio <a id="desafio"></a>

Aplicar e corrigir avaliações escolares é um processo que envolve várias etapas manuais e sujeitas a erro: criar questões de diferentes tipos (múltipla escolha, associação e discursivas), organizar essas questões em provas com pesos definidos, liberar o acesso apenas às turmas e nos prazos corretos, impedir respostas fora do prazo, corrigir automaticamente o que é possível e reservar a correção manual apenas para as questões discursivas.

Sem um sistema que automatize esse fluxo, o professor precisa controlar manualmente:
- Quais turmas e alunos têm acesso a cada avaliação e em qual período;
- A correção de questões objetivas (múltipla escolha e associação), item a item;
- O cálculo da nota de cada avaliação e da média final, ponderada pelos pesos de cada prova;
- O histórico de turmas concluídas e o desempenho dos alunos ao longo do curso;
- A segurança da avaliação, já que respostas em uma ordem fixa facilitam o compartilhamento de gabarito entre alunos.

Além disso, uma avaliação já respondida por algum aluno não pode ser alterada livremente, sob risco de comprometer a nota de quem já a realizou — o que exige um controle de versionamento das provas.

---

### 💡 Solução - SNCTECH <a id="solução"></a>

A SNCTECH busca automatizar o ciclo completo de uma avaliação escolar, do cadastro das questões pelo professor até a divulgação da média final ao aluno.

O sistema permite que o professor crie avaliações com questões de múltipla escolha, associação e discursivas, podendo adicionar imagens aos enunciados e definir o peso de cada prova. Após uma avaliação ser respondida, qualquer alteração gera uma nova versão, mantendo a versão original para preservar as respostas dos alunos.

Na aplicação da prova, as alternativas das questões objetivas são embaralhadas a cada acesso do aluno, dificultando o compartilhamento do gabarito. A avaliação só fica disponível após a liberação do professor e deve ser respondida dentro do prazo definido; caso contrário, a nota é registrada automaticamente como 0.

A correção de questões de múltipla escolha e associação é automática, com base no gabarito informado pelo professor; questões discursivas são corrigidas manualmente, avaliação por avaliação. A nota só se torna "nota final" depois que todas as questões discursivas forem validadas, e a média final é calculada automaticamente, ponderando as notas pelos pesos de cada avaliação (≥ 6: aprovado; < 4: reprovado; faixa intermediária: EXF).

O professor também gerencia turmas e alunos, encerrando automaticamente a turma atual ao cadastrar uma nova, sem perder o histórico. O aluno pode acompanhar suas notas, média final e avaliações futuras, sem acessar as questões antes da liberação. Todo o acesso é controlado por login, conforme o perfil do usuário.

---
### 📋 Backlog do Produto <a id="backlog"></a>
| ID | Épico | User Story | Prioridade | Status |
|:--:|-------|-------------|:----------:|:------:|
| **PB-01** | Gerenciamento de Avaliação | Como professor, desejo gerenciar as informações das avaliações para reutilizar avaliações já aplicadas e criar novas avaliações. | 🔴 Alta | ❌ Não finalizado |
| **PB-02** | Gerenciamento de questões | Como professor, desejo gerenciar questões para centralizar elas em um único lugar. | 🔴 Alta | ❌ Não finalizado |
| **PB-03** | Correção Manual | Como professor, desejo a correção manual para corrigir questões discursivas e verificar a nota final. | 🔴 Alta | ❌ Não finalizado |
| **PB-04** | Embaralhamento de Respostas | Como professor, desejo que as respostas de questões de múltipla escolha e associação sejam embaralhadas para reduzir a possibilidade de compartilhamento da ordem das respostas entre os alunos. | 🟡 Média | ❌ Não finalizado |
| **PB-05** | Disponibilização de Avaliação | Como professor, desejo disponibilizar uma avaliação para uma ou mais turmas para que os alunos possam ter acesso a ela. | 🟡 Média | ❌ Não finalizado |
| **PB-06** | Realização das avaliações | Como professor, desejo que os alunos realizem a prova para centralizar todas as avaliações. | 🟡 Média | ❌ Não finalizado |
| **PB-07** | Realização das avaliações | Como aluno, desejo realizar as avaliações para registrar minhas respostas e ser avaliado. | 🟡 Média | ❌ Não finalizado |
| **PB-08** | Correção Automática | Como professor, desejo uma correção automática para as questões de múltipla escolha e assimilação para facilitar a correção das avaliações. | 🟡 Média | ❌ Não finalizado |
| **PB-09** | Gerenciamento de Turmas | Como professor, desejo cadastrar e editar turmas ativas e já concluídas para gerenciamento e organização das avaliações. | 🟡 Média | ❌ Não finalizado |
| **PB-10** | Gerenciamento de Alunos | Como professor, desejo adicionar, editar e visualizar os alunos pertencentes a minhas turmas ativas para organização das minhas turmas. | 🟡 Média | ❌ Não finalizado |
| **PB-11** | Visualização Avaliação | Como aluno, desejo visualizar minhas avaliações já feitas e as futuras para de ter controle da minhas notas e planos de estudo. | 🟡 Média | ❌ Não finalizado |
| **PB-12** | Notas | Como professor, desejo que o cálculo das notas seja gerado automaticamente para facilitar a correção. | 🟢 Baixa | ❌ Não finalizado |
| **PB-13** | Notas | Como aluno, desejo visualizar minhas notas para acompanhar meu desempenho. | 🟢 Baixa | ❌ Não finalizado |
| **PB-14** | Notas | Como professor, desejo visualizar as notas dos alunos para ter controle de aprovação e desempenho. | 🟢 Baixa | ❌ Não finalizado |
| **PB-15** | Média Final | Como aluno, desejo visualizar minha média final para ter conhecimento da minha aprovação ou reprovação. | 🟢 Baixa | ❌ Não finalizado |
| **PB-16** | Média Final | Como professor, desejo que o cálculo da média seja feito de forma automática para facilitar o acesso a essa informação. | 🟢 Baixa | ❌ Não finalizado |
| **PB-17** | Média Final | Como professor, desejo visualizar as médias finais dos alunos para ter controle de aprovação e desempenho. | 🟢 Baixa | ❌ Não finalizado |
| **PB-18** | Prazo Das Avaliações | Como professor, desejo definir um prazo para as avaliações a fim de evitar que alunos façam antes da hora ou depois do prazo. | 🟢 Baixa | ❌ Não finalizado |
| **PB-19** | Autenticação | Como usuário, desejo realizar login no sistema para acessar as funcionalidades correspondentes ao meu perfil. | 🟢 Baixa | ❌ Não finalizado |
 
> Legenda — Prioridade: 🔴 Alta · 🟡 Média · 🟢 Baixa. <BR>Status: ✅ Concluído · ❌ Não finalizado.
>


---

### 📆 Cronograma das Sprints <a id="sprints"></a>

<div> <table> <tr> <th>Etapa</th> <th>Período</th> <th>Status</th> </tr> <tr> <td><b>Início das Aulas</b></td> <td align="center">03/08</td> <td align="center">✅</td> </tr> <tr> <td><b>Kick-off geral</b></td> <td align="center">24/08 a 28/08</td> <td align="center">✅</td> </tr> <tr> <td><b>Construção do Backlog de Produto / Planning</b></td> <td align="center">31/08 a 04/09</td> <td align="center">✅</td> </tr> <tr> <td><b>Sprint 1</a></b></td> <td align="center">07/09 a 27/09</td> <td align="center">🔄</td> </tr> <tr> <td><i>Sprint Review / Planning</i></td> <td align="center">28/09 a 02/10</td> <td align="center">🔲</td> </tr> <tr> <td><b>Sprint 2</a></b></td> <td align="center">05/10 a 25/10</td> <td align="center">🔲</td> </tr> <tr> <td><i>Sprint Review / Planning</i></td> <td align="center">26/10 a 30/10</td> <td align="center">🔲</td> </tr> <tr> <td><b>Sprint 3</a></b></td> <td align="center">02/11 a 22/11</td> <td align="center">🔲</td> </tr> <tr> <td><i>Sprint Review</i></td> <td align="center">23/11 a 27/11</td> <td align="center">🔲</td> </tr> <tr> <td><b>Feira de Soluções</b></td> <td align="center">03/12</td> <td align="center">🔲</td> </tr> <tr> <td><b>Apresentação de TGs</b></td> <td align="center">07/12 a 11/12</td> <td align="center">🔲</td> </tr> <tr> <td><b>Encerramento das Aulas</b></td> <td align="center">14/12</td> <td align="center">🔲</td> </tr> </table> </div>

> 🔲 pendente · 🔄 em andamento · ✅ concluído 

---

### 📄 Documentação <a id="documentacao"></a>

A documentação completa pode ser consultada na pasta <a href="/Documentos/">Documentos</a>.

##### Conteúdo:
- <a href="https://github.com/SNCTech-FATEC-SJC/API_2Semestre/blob/main/Documenta%C3%A7%C3%A3o/Backlog.MD">Product Backlog completo</a>
- <a href="https://github.com/SNCTech-FATEC-SJC/API_2Semestre/blob/main/Documenta%C3%A7%C3%A3o/Definition%20of%20Ready.MD">Definition of Ready (DoR)</a>
- <a href="https://github.com/SNCTech-FATEC-SJC/API_2Semestre/blob/main/Documenta%C3%A7%C3%A3o/Definition%20of%20Done.MD">Definition of Done (DoD)</a>





---

### 💻 Tecnologias <a id="tecnologias"></a>

> _A preencher pelo grupo conforme a stack definida._

- **Linguagem/Framework da API:** _a definir_
- **Banco de dados:** _a definir_
- **ORM / ferramenta de acesso a dados:** _a definir_
- **Autenticação:** _a definir_
- **Versionamento:** Git <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/git/git-original.svg" width="20" height="20" alt="Git"/>
- **Outras ferramentas:** _a definir_

---

### 👥 Equipe <a id="equipe"></a>
<div align="center"> <table> <tr> <th>Membro</th> <th>Função</th> <th>Github</th> </tr> <tr> <td>Israel Mesquita Cardoso</td> <td>Product Owner</td> <td><a href="https://github.com/IsraelMCardoso"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td> </tr> <tr> <td>Asafe Maximiano dos Santos</td> <td>Scrum Master</td> <td><a href="https://github.com/AsafeMax2008"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td> </tr> <tr> <td>Pietro Ramos Silveira</td> <td>Dev Team</td> <td><a href="https://github.com/Zanella08"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td> </tr> <tr> <td>Helder Francisco da Costa</td> <td>Dev Team</td> <td><a href="https://github.com/helderfcosta"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td> </tr> <tr> <td>Miguel Tediole de Oliveira</td> <td>Dev Team</td> <td><a href="https://github.com/tediolem"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td> </tr> <tr> <td>Yan Vitor Siqueira Bergantin</td> <td>Dev Team</td> <td><a href="https://github.com/YanBergantin"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td> </tr> </table> </div>


---


