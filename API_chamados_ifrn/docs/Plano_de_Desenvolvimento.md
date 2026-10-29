# Plano de desenvolvimento — API de Chamados IFRN

Este plano organiza os requisitos de [Projeto_API_Sistema_de_Chamados_IFRN.md](Projeto_API_Sistema_de_Chamados_IFRN.md) em etapas de estudo e implementação.

Em cada etapa, vamos explicar o conceito, fazer uma mudança pequena, validar o resultado e revisá-lo. A próxima etapa começa após a aprovação do estudante.

## Situação inicial

O projeto já possui as cinco entidades, os enums, os repositórios, duas migrations Flyway, a configuração Docker e um `SetorService`. Ainda faltam os controllers, os demais services, as regras dos chamados e a documentação de execução da API.

## Etapas

### 1. Ambiente e banco

- Revisar Java 21, Docker Compose, conexão com PostgreSQL e as migrations existentes.
- Entender a relação entre Docker, Flyway e Hibernate.
- Validar a execução das migrations V1 e V2, os dados iniciais e a configuração `ddl-auto=validate`.

### 2. Padrão das respostas

- Preparar DTOs, validação dos dados de entrada e tratamento global de erros.
- Entender o papel dos DTOs e os códigos HTTP **400**, **404** e **409**.
- Verificar o formato de erro com `timestamp`, `status`, `mensagem` e `caminho`.

### 3. Setores

- Criar os endpoints de cadastro, listagem, consulta por ID, atualização e exclusão, aproveitando o `SetorService` existente.
- Acompanhar o caminho da requisição entre controller, service e repository.
- Testar o filtro por nome, a paginação e o bloqueio da exclusão quando houver vínculos.

### 4. Categorias

- Criar o service e os endpoints de cadastro, listagem, consulta por ID, atualização e exclusão.
- Implementar `GET /setores/{id}/categorias` e os filtros por nome e setor.
- Trabalhar relacionamentos por ID e verificar o bloqueio da exclusão de categoria com chamados vinculados.

### 5. Usuários

- Criar o service e os endpoints de cadastro, listagem, consulta por ID e atualização.
- Validar e-mail, tipo de usuário e setor do técnico.
- Testar os filtros por nome, tipo e setor e o retorno **409** para e-mail duplicado.

### 6. Abertura de chamados

- Implementar `POST /chamados`.
- Aplicar no service as regras de abertura: somente solicitante pode abrir, o status inicial é `ABERTO`, a data é preenchida pelo sistema e a prioridade padrão é `MEDIA`.
- Validar tanto a abertura correta quanto as tentativas com dados ou usuário inválidos.

### 7. Consulta de chamados

- Implementar a listagem, o detalhe e a listagem de chamados por usuário.
- Combinar os filtros de status, prioridade, setor, categoria, solicitante, técnico e período de abertura; na listagem por usuário, permitir filtro por status.
- Usar `Pageable` e verificar os itens e metadados. Toda listagem deve ter padrão de **10 itens** e limite de **50**.

### 8. Assumir chamado

- Implementar `PATCH /chamados/{id}/assumir`.
- Verificar se o chamado está `ABERTO` e se o técnico pertence ao setor da categoria.
- Confirmar a atribuição do técnico e a mudança para `EM_ATENDIMENTO`; testar as recusas.

### 9. Resolver chamado

- Implementar `PATCH /chamados/{id}/resolver`.
- Permitir a resolução somente ao técnico atribuído, exigir solução e preencher a data de fechamento.
- Testar a transição para `RESOLVIDO` e as tentativas inválidas.

### 10. Cancelar chamado

- Implementar `PATCH /chamados/{id}/cancelar`.
- Permitir o cancelamento somente ao próprio solicitante enquanto o chamado estiver `ABERTO`.
- Testar o cancelamento correto, por outro usuário e após o início do atendimento.

### 11. Interações

- Implementar o registro de interações e incluí-las no detalhe do chamado.
- Permitir interação ao solicitante e ao técnico atribuído.
- Bloquear novas interações e alterações em chamados `RESOLVIDO` ou `CANCELADO`.

### 12. Documentação e entrega

- Criar o `README.md` da API e um arquivo `.http` com todas as requisições exigidas.
- Explicar no README como o Flyway registra versões, como o checksum detecta alterações em migrations aplicadas e por que usar `ddl-auto=validate`.
- Validar o fluxo completo com `docker compose up --build`, revisar o histórico de commits e preparar o repositório público para entrega na `main`.

## Identificação de quem executa uma ação

O projeto não prevê autenticação. Nas ações de chamado, o corpo da requisição informará o usuário responsável, por exemplo com `solicitanteId`, `tecnicoId` ou `autorId`. Os services verificarão as regras correspondentes.
