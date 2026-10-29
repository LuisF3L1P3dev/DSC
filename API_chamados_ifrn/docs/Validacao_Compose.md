# Validação pelo Docker Compose

Subponto 12.3 do plano de desenvolvimento, executado em **29/10/2026**.

## Inicialização

Foi executado, na pasta da API:

```powershell
docker compose up --build -d
```

O build terminou com `BUILD SUCCESS`, e os serviços `api` e `db` ficaram em execução. A aplicação iniciou com Java **21.0.13** e conectou ao PostgreSQL **17.11**. A consulta `GET /setores?page=0&size=10` pela porta local 8080 retornou HTTP 200.

## Flyway e Hibernate

O volume do banco já continha as migrations aplicadas. Nesta execução, o Flyway validou as duas versões e informou que o esquema estava atualizado, sem reaplicá-las.

Consulta à tabela `flyway_schema_history`:

| Versão | Descrição | Checksum | Sucesso |
| --- | --- | --- | --- |
| 1 | criar tabelas | 79610435 | true |
| 2 | inserir dados iniciais | 63719140 | true |

Com `spring.jpa.hibernate.ddl-auto=validate`, o Hibernate inicializou a persistência e a API iniciou sem erro de incompatibilidade do esquema.

## Execução da coleção

As 39 requisições de [requisicoes.http](../requisicoes.http) foram executadas na ordem pelo HTTP Client CLI 2026.2 da JetBrains, em container. A opção `-D` permitiu acessar a API publicada no host por `localhost:8080`.

Uma cópia temporária da coleção recebeu verificações de códigos HTTP e conteúdo das respostas. As requisições, os dados enviados e os scripts de captura de variáveis foram preservados. Os 39 códigos recebidos corresponderam aos indicados no arquivo:

| Código esperado e recebido | Quantidade | Uso na coleção |
| --- | --- | --- |
| 200 | 22 | Consultas, atualizações e ações de chamados |
| 201 | 10 | Cadastros, abertura de chamados e interações |
| 204 | 2 | Exclusão de categoria e setor sem vínculos |
| 400 | 1 | Título obrigatório em branco |
| 404 | 1 | Chamado inexistente |
| 409 | 3 | E-mail duplicado e exclusões bloqueadas |
| **Total** | **39** | **Todos os 23 endpoints exigidos foram exercitados** |

As cinco respostas de erro também foram conferidas como JSON bruto: todas continham exatamente `timestamp`, `status`, `mensagem` e `caminho`. A comparação adicional das chaves no verificador temporário precisou de ajuste; os cinco casos foram reexecutados com verificação direta dos campos e passaram.

## Conteúdo e estado final conferidos

- Os cadastros foram atualizados e consultados pelos IDs capturados das respostas.
- A listagem de usuários combinou nome, tipo e setor e encontrou o técnico criado.
- A listagem de chamados combinou os oito filtros e encontrou o chamado do fluxo.
- As listagens por solicitante e técnico retornaram o chamado em atendimento.
- As respostas paginadas apresentaram itens, número da página, tamanho e totais.
- O chamado **3** terminou como `RESOLVIDO`, com solução, data de fechamento e duas interações em ordem: uma do solicitante e outra do técnico atribuído.
- O chamado **4** terminou como `CANCELADO`; a abertura sem prioridade explícita resultou em `MEDIA`.
- A categoria descartável **7** e o setor descartável **6** foram excluídos com 204; consultas posteriores retornaram 404.
- Swagger UI respondeu com 200 e o OpenAPI descreveu os 23 endpoints.

Os usuários e chamados de demonstração permanecem no banco local, conforme explicado no README. A API e o PostgreSQL ficaram em execução ao final da validação.

## Arquivos relacionados

- [README da API](../README.md).
- [Coleção de requisições](../requisicoes.http).
- [Plano de desenvolvimento](Plano_de_Desenvolvimento.md).
