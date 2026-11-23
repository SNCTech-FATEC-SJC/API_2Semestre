
# SNCTECH - Gerenciamento e Aplicação de Avaliações Escolares
## Projeto API - 2º Semestre 2026
<p align="center">
  <img src="Documentação/logo_snctech.png" alt="Logo SNCTECH" width="400"/>
</p>
<p align="center">
    <a href="#sobre"> Sobre</a> |
    <a href="#desafio"> Desafio</a> |
    <a href="#solução"> Solução</a> |
    <a href="#backlog"> Backlog do Produto</a> |
    <a href="#sprints"> Cronograma das Sprints</a> |
    <a href="#documentacao"> Documentação</a> |
    <a href="#manual"> Manual do Usuário</a> |
   <a href="#instalacao"> Manual de Instalação</a> |
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

Na aplicação da prova, as alternativas das questões objetivas são embaralhadas a cada acesso do aluno, dificultando o compartilhamento do gabarito. A avaliação só fica disponível após a liberação do professor e deve ser respondida dentro do prazo definido; ao chegar a 00:00, é entregue automaticamente com as respostas já salvas. Avaliações não realizadas recebem nota 0.

A correção de questões de múltipla escolha e associação é automática, com base no gabarito informado pelo professor; questões discursivas são corrigidas manualmente, avaliação por avaliação. A nota só se torna "nota final" depois que todas as questões discursivas forem validadas, e a média final é calculada automaticamente, ponderando as notas pelos pesos de cada avaliação (≥ 6: aprovado; < 4: reprovado; faixa intermediária: EXF).

O professor também gerencia turmas e alunos, encerrando automaticamente a turma atual ao cadastrar uma nova, sem perder o histórico. O aluno pode acompanhar suas notas, média final e avaliações futuras, sem acessar as questões antes da liberação. Todo o acesso é controlado por login, conforme o perfil do usuário.

Ao atingir 00:00, a avaliação é entregue automaticamente com as respostas registradas até o limite. Ao concluir uma turma, o sistema calcula as médias ponderadas e cria ou reutiliza uma turma de recuperação para alunos com média estritamente maior que 4 e menor que 6. Um aluno já associado a uma recuperação não é incluído em outra. Esses registros usam as tabelas existentes de turmas e matrículas.

---
### 📋 Backlog do Produto <a id="backlog"></a>
| ID | Épico | User Story | Status |
|:--:|-------|-------------|:------:|
| **PB-01** | Gerenciamento de Avaliação | Como professor, desejo gerenciar as informações das avaliações para reutilizar avaliações já aplicadas e criar novas avaliações. | ✅ Concluído |
| **PB-02** | Gerenciamento de questões | Como professor, desejo gerenciar questões para centralizar elas em um único lugar. | ✅ Concluído |
| **PB-03** | Correção Manual | Como professor, desejo a correção manual para corrigir questões discursivas e verificar a nota final. | ✅ Concluído |
| **PB-04** | Embaralhamento de Respostas | Como professor, desejo que as respostas de questões de múltipla escolha e associação sejam embaralhadas para reduzir a possibilidade de compartilhamento da ordem das respostas entre os alunos. | ✅ Concluído |
| **PB-05** | Disponibilização de Avaliação | Como professor, desejo disponibilizar uma avaliação para uma ou mais turmas para que os alunos possam ter acesso a ela. | ✅ Concluído |
| **PB-06** | Realização das avaliações | Como professor, desejo que os alunos realizem a prova para centralizar todas as avaliações. | ✅ Concluído |
| **PB-07** | Realização das avaliações | Como aluno, desejo realizar as avaliações para registrar minhas respostas e ser avaliado. | ✅ Concluído |
| **PB-08** | Correção Automática | Como professor, desejo uma correção automática para as questões de múltipla escolha e assimilação para facilitar a correção das avaliações. | ✅ Concluído |
| **PB-09** | Gerenciamento de Turmas | Como professor, desejo cadastrar e editar turmas ativas e já concluídas para gerenciamento e organização das avaliações. | ✅ Concluído |
| **PB-10** | Gerenciamento de Alunos | Como professor, desejo adicionar, editar e visualizar os alunos pertencentes a minhas turmas ativas para organização das minhas turmas. | ✅ Concluído |
| **PB-11** | Visualização Avaliação | Como aluno, desejo visualizar minhas avaliações já feitas e as futuras para de ter controle da minhas notas e planos de estudo. | ✅ Concluído |
| **PB-12** | Notas | Como professor, desejo que o cálculo das notas seja gerado automaticamente para facilitar a correção. | ✅ Concluído |
| **PB-13** | Notas | Como aluno, desejo visualizar minhas notas para acompanhar meu desempenho. | ✅ Concluído |
| **PB-14** | Notas | Como professor, desejo visualizar as notas dos alunos para ter controle de aprovação e desempenho. | ✅ Concluído |
| **PB-15** | Média Final | Como aluno, desejo visualizar minha média final para ter conhecimento da minha aprovação ou reprovação. | ✅ Concluído |
| **PB-16** | Média Final | Como professor, desejo que o cálculo da média seja feito de forma automática para facilitar o acesso a essa informação. | ✅ Concluído |
| **PB-17** | Média Final | Como professor, desejo visualizar as médias finais dos alunos para ter controle de aprovação e desempenho. | ✅ Concluído |
| **PB-18** | Prazo Das Avaliações | Como professor, desejo definir um prazo para as avaliações a fim de evitar que alunos façam antes da hora ou depois do prazo. | ✅ Concluído |
| **PB-19** | Autenticação | Como usuário, desejo realizar login no sistema para acessar as funcionalidades correspondentes ao meu perfil. | ✅ Concluído |
 
> Legenda — Prioridade: 🔴 Alta · 🟡 Média · 🟢 Baixa. <BR>Status: ✅ Concluído · ❌ Não finalizado.
>


---

### 📆 Cronograma das Sprints <a id="sprints"></a>

<div> <table> <tr> <th>Etapa</th> <th>Período</th> <th>Status</th> </tr> <tr> <td><b>Início das Aulas</b></td> <td align="center">03/08</td> <td align="center">✅</td> </tr> <tr> <td><b>Kick-off geral</b></td> <td align="center">24/08 a 28/08</td> <td align="center">✅</td> </tr> <tr> <td><b>Construção do Backlog de Produto / Planning</b></td> <td align="center">31/08 a 04/09</td> <td align="center">✅</td> </tr> <tr> <td><b>Sprint 1</a></b></td> <td align="center">07/09 a 27/09</td> <td align="center">✅</td> </tr> <tr> <td><i>Sprint Review / Planning</i></td> <td align="center">28/09 a 02/10</td> <td align="center">✅</td> </tr> <tr> <td><b>Sprint 2</a></b></td> <td align="center">05/10 a 25/10</td> <td align="center">✅</td> </tr> <tr> <td><i>Sprint Review / Planning</i></td> <td align="center">26/10 a 30/10</td> <td align="center">✅</td> </tr> <tr> <td><b>Sprint 3</a></b></td> <td align="center">02/11 a 22/11</td> <td align="center">✅</td> </tr> <tr> <td><i>Sprint Review</i></td> <td align="center">23/11 a 27/11</td> <td align="center">🔲</td> </tr> </table> </div>

> 🔲 pendente · 🔄 em andamento · ✅ concluído 

---

### 📄 Documentação <a id="documentacao"></a>

A documentação completa está na pasta [Documentação](<./Documentação/>).

##### Conteúdo:
- [Product Backlog completo](<./Documentação/Backlog.MD>)
- [Critérios de Permanência](<./Documentação/Criterios_de_Permanencia.md>)
- [Definition of Ready (DoR)](<./Documentação/Definition of Ready.MD>)
- [Definition of Done (DoD)](<./Documentação/Definition of Done.MD>)
- [Estratégia de Branch](<./Documentação/Estratégia de Branch.md>)
- [Manual de Instalação](<./Documentação/Manual de Instalação.md>)
-  [Manual do Usuário](<./Documentação/Manual do Usuário.md>)


---

### 📖 Manual do usuário <a id="manual"></a>

Consulte o [Manual do Usuário](<./Documentação/Manual do Usuário.md>) para obter orientações de uso da aplicação.


---

### 📖 Manual do usuário <a id="instalacao"></a>

Consulte o [Manual de Instalação](<./Documentação/Manual de Instalação.md>) para obter orientações sobre o processo de instalção

---

### 💻 Tecnologias <a id="tecnologias"></a>


- **Linguagem de programação:** Java 25
- **Interface gráfica:** JavaFX 25.0.5, FXML e CSS
- **Banco de dados:** PostgreSQL <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/postgresql/postgresql-original.svg" alt="postgresql" width="20" height="20"/>
- **Acesso ao banco de dados:** JDBC
- **Testes automatizados:** JUnit 5 
- **Gerenciamento de dependências:** Maven
- **Versionamento:** Git <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/git/git-original.svg" width="20" height="20" alt="Git"/>
- **Ferramenta de interface:** Scene Builder

---

### 👥 Equipe <a id="equipe"></a>
<div align="center"> <table> <tr> <th>Membro</th> <th>Função</th> <th>Github</th> </tr> <tr> <td>Israel Mesquita Cardoso</td> <td>Product Owner</td> <td><a href="https://github.com/IsraelMCardoso"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td> </tr> <tr> <td>Asafe Maximiano dos Santos</td> <td>Scrum Master</td> <td><a href="https://github.com/AsafeMax2008"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td> </tr> <tr> <td>Pietro Ramos Silveira</td> <td>Dev Team</td> <td><a href="https://github.com/Zanella08"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td> </tr> <tr> <td>Helder Francisco da Costa</td> <td>Dev Team</td> <td><a href="https://github.com/helderfcosta"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td> </tr> <tr> <td>Miguel Tediole de Oliveira</td> <td>Dev Team</td> <td><a href="https://github.com/tediolem"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td> </tr> <tr> <td>Yan Vitor Siqueira Bergantin</td> <td>Dev Team</td> <td><a href="https://github.com/YanBergantin"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"></a></td> </tr> </table> </div>


---


