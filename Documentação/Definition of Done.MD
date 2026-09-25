# ✅ Definition of Done (DoD)

**Equipe:** SNCTECH

\---

## DoD Geral



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

\---

## DoD Específico por Épico (PBS)

|ID|Requisitos de Entrega e Conclusão (DoD)|
|-|-|
|**PB-01**|• Versões anteriores devem permanecer disponíveis para consulta.|
|**PB-03**|• O professor pode alterar uma correção já realizada.|
|**PB-04**|• As alternativas devem aparecer em ordem diferente para diferentes alunos.<br><br />• A identificação da resposta correta mantém-se ao conteúdo e não à sua posição visual.<br><br />• O sistema deve validar a alternativa correta após o embaralhamento.<br><br />• Não é preciso garantir uma ordem diferente para todos os alunos quando não houver possibilidades suficientes.|
|**PB-05**|• O acesso é bloqueado antes do início.<br><br />• Ao atingir o horário de término, os alunos não conseguem alterar ou enviar as respostas.<br>• Respostas já registradas devem ser preservadas.<br><br />• Após o início da avaliação, nenhuma alteração nela deve ser permitida.<br><br />• O encerramento ocorre automaticamente ao atingir o horário definido.|
|**PB-07**|• Respostas devem ser salvas durante a realização.<br><br />• Aluno que não iniciar até o encerramento recebe nota 0.<br><br />• Aluno que iniciar, mas não concluir, deve ter suas respostas já registradas preservadas.<br>• Questões não respondidas pelo aluno são consideradas incorretas (0 pontos).<br><br />• O aluno não pode alterar respostas após o encerramento.|
|**PB-08**|• Questões objetivas incorretas recebem 0 inicialmente.<br><br />• A pontuação pode ser alterada pelo professor por correção manual.<br><br />• Questões de associação recebem nota parcial proporcional à quantia de associações corretas.<br><br />• A correção respeita a alternativa correta independente da posição.|
|**PB-15**|• A média só é liberada após todas as avaliações previstas estarem encerradas e corrigidas.<br><br />• A situação acadêmica é calculada a partir da média final.<br><br />• Regras de aprovação:<br>  - `Nota >= 6`: Aprovado<br>  - `4 <= Nota < 6`: Exame Final<br>  - `Nota < 4`: Reprovado|
