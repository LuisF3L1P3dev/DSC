# API de Chamados IFRN

API REST para registrar e acompanhar chamados de suporte no IFRN. O sistema organiza setores, categorias e usuários, permite abrir, assumir, resolver e cancelar chamados e mantém o histórico de interações.

O projeto usa Java 21, Spring Boot 4, Maven, Spring Data JPA/Hibernate, Bean Validation, Flyway e PostgreSQL 17.

## Executar com Docker Compose

É necessário ter Docker com Docker Compose disponível e as portas locais **8080** e **5432** livres. O build usa Maven com JDK 21 dentro do Docker; a execução da API usa JRE 21.

Abra um terminal na pasta `API_chamados_ifrn`, onde está o [docker-compose.yml](docker-compose.yml), e execute:

```powershell
docker compose up --build -d
```

O comando constrói a imagem da API e inicia os serviços `db` e `api`. O Compose aguarda o PostgreSQL ficar saudável antes de iniciar a API. Durante a inicialização da aplicação, o Flyway aplica as migrations pendentes e o Hibernate valida o esquema.

Para acompanhar a inicialização:

```powershell
docker compose ps
docker compose logs -f api
```

Quando aparecer a mensagem de aplicação iniciada (`Started ApiChamadosIfrnApplication`), acesse:

- API: `http://localhost:8080` como URL base; por exemplo, [GET /setores](http://localhost:8080/setores).
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).
- OpenAPI em JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).

### Exemplos de consulta no PowerShell

Listar setores:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/setores?page=0&size=10"
```

Listar chamados abertos:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/chamados?status=ABERTO&page=0&size=10"
```

As páginas começam em `0`. O tamanho padrão é de 10 itens e o limite é de 50. Em um banco novo, a listagem de chamados estará vazia até que um chamado seja aberto.

### Coleção de requisições `.http`

Abra [requisicoes.http](requisicoes.http) no HTTP Client do IntelliJ IDEA. Com a API iniciada, execute os blocos de cima para baixo, um por vez, usando o botão de execução ao lado de cada requisição. Confira o código HTTP indicado no título antes de seguir; os blocos marcados como erro esperado demonstram os retornos 400, 404 e 409.

O arquivo cobre todos os endpoints exigidos e seus filtros. Ele cria os próprios dados de exemplo e usa scripts de resposta para capturar IDs e e-mail, reutilizados nos blocos seguintes por variáveis como `{{exemploChamadoId}}`. A sintaxe segue a [documentação do HTTP Client](https://www.jetbrains.com/help/idea/http-client-reference.html); as variáveis dinâmicas geram siglas e e-mails novos em cada execução, conforme a [referência de variáveis](https://www.jetbrains.com/help/idea/http-client-variables.html).

A sequência inclui cadastro e atualização, abertura de chamado, interações, atendimento, resolução e um segundo chamado para cancelamento. As exclusões com sucesso usam uma categoria e um setor descartáveis, criados no fim da coleção. Os usuários e os chamados de demonstração permanecem no banco após a execução.

### Parar os serviços

```powershell
docker compose down
```

O volume nomeado `postgres_data` mantém os dados após a remoção dos containers. Ao iniciar novamente os serviços com o mesmo volume, as migrations já aplicadas permanecem registradas.

## Configuração do banco

O Compose define os seguintes valores padrão para desenvolvimento local:

| Variável do Compose | Padrão |
| --- | --- |
| `POSTGRES_DB` | `chamados` |
| `POSTGRES_USER` | `ifrn` |
| `POSTGRES_PASSWORD` | `ifrn_dev` |

A API recebe a conexão pelas variáveis `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`, configuradas no serviço `api` do Compose. Com os padrões acima, a URL dentro da rede Docker é `jdbc:postgresql://db:5432/chamados`. O nome `db` identifica o serviço PostgreSQL nessa rede.

O [application.properties](src/main/resources/application.properties) contém as configurações de validação do esquema e paginação. As credenciais de conexão são fornecidas pelo ambiente.

## Migrations e validação do esquema

As migrations estão em [src/main/resources/db/migration](src/main/resources/db/migration):

| Arquivo | Responsabilidade |
| --- | --- |
| [V1__criar_tabelas.sql](src/main/resources/db/migration/V1__criar_tabelas.sql) | Cria as cinco tabelas, os relacionamentos e as restrições de integridade. |
| [V2__inserir_dados_iniciais.sql](src/main/resources/db/migration/V2__inserir_dados_iniciais.sql) | Insere dois setores, quatro categorias e quatro usuários. |

O nome segue o padrão `V<versão>__<descrição>.sql`, com dois sublinhados entre a versão e a descrição. Cada mudança do banco deve entrar em uma nova versão, como `V3__descricao_da_mudanca.sql`.

### Como o Flyway registra as versões

O Flyway mantém a tabela `flyway_schema_history` no banco. Ela registra informações como versão, descrição, checksum e resultado da execução. As migrations versionadas pendentes são executadas em ordem, uma vez por banco; nas próximas inicializações, o histórico identifica o que já foi aplicado. Consulte a documentação de [migrations versionadas do Flyway](https://documentation.red-gate.com/fd/versioned-migrations-273973333.html).

Para consultar o histórico usando os valores padrão do Compose:

```powershell
docker compose exec db psql -U ifrn -d chamados -c "SELECT version, description, checksum, success FROM flyway_schema_history ORDER BY installed_rank;"
```

Após a primeira inicialização bem-sucedida, o histórico deve conter as versões `1` e `2` com `success = true`.

### O que acontece se uma migration aplicada mudar

O checksum é um valor calculado a partir do conteúdo da migration. O Flyway compara o arquivo atual com o valor registrado no histórico; uma divergência faz a validação falhar e impede a inicialização normal da API. Por isso, uma migration já aplicada deve ser preservada: uma correção entra em outra versão. Consulte a documentação de [validação do Flyway](https://documentation.red-gate.com/flyway/reference/commands/validate).

### Por que usar `ddl-auto=validate`

O projeto define:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

O Hibernate verifica a compatibilidade entre as entidades JPA e o esquema existente. Se houver incompatibilidade, a inicialização falha. As mudanças de estrutura ficam sob responsabilidade das migrations Flyway.

Com `ddl-auto=update`, o Hibernate tentaria alterar o esquema automaticamente, sem produzir uma migration SQL versionada para revisar e repetir nos demais ambientes. Usar Flyway para evoluir o banco e `validate` para conferir o resultado mantém as alterações explícitas no repositório. Veja a orientação do Spring Boot sobre [inicialização do banco](https://docs.spring.io/spring-boot/how-to/data-initialization.html).

## Dados iniciais

Em um banco criado do zero, a V2 cadastra os setores COTIC e Manutenção, as categorias Rede, Computadores, Elétrica e Hidráulica e os seguintes usuários:

| ID | Nome | Tipo | Setor |
| --- | --- | --- | --- |
| 1 | Ana | `SOLICITANTE` | — |
| 2 | Bruno | `SOLICITANTE` | — |
| 3 | Carlos | `TECNICO` | COTIC |
| 4 | Daniela | `TECNICO` | Manutenção |

O projeto não prevê autenticação. As ações dos chamados identificam o usuário responsável no corpo da requisição, com `solicitanteId`, `tecnicoId` ou `autorId`; os services verificam as regras correspondentes.

## Documentos do projeto

- [Requisitos do sistema](docs/Projeto_API_Sistema_de_Chamados_IFRN.md).
- [Plano de desenvolvimento](docs/Plano_de_Desenvolvimento.md).
- [Resultado da validação pelo Compose](docs/Validacao_Compose.md).
