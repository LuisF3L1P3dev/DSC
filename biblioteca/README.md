# Sistema de Biblioteca com Spring Data JPA

Projeto educacional que implementa o modelo lógico de uma biblioteca usando Spring Boot,
Spring Data JPA e H2.

O escopo atual contém entidades, repositories, services de consulta, API REST, Swagger
UI, dados de demonstração e testes automatizados.

## Organização das pastas

```text
src/main/java/br/edu/ifrn/biblioteca
|- config/                 Beans de infraestrutura usados pela aplicação
|- model/                  Entidades JPA e enumerações do domínio
|- repository/             Interfaces que abstraem o acesso ao banco
|  `- projection/          Formatos internos de resultados agregados
|- service/                Contratos dos casos de uso de consulta
|  `- impl/                Implementações dos contratos
|- web/                    Controllers, DTOs, mapeadores e tratamento de erros HTTP
`- BibliotecaApplication  Ponto de entrada do Spring Boot

src/main/resources
`- application.properties Configuração do H2 e do Hibernate

src/test
|- java/                   Testes de repository, service e contexto
`- resources/              Configuração isolada do banco de testes
```

### `model`

Cada classe anotada com `@Entity` representa uma tabela. `@Id` identifica a chave
primária e `@GeneratedValue` delega ao banco a geração do ID.

- `Livro` pertence a uma `Categoria` por `@ManyToOne`.
- `Livro` e `Autor` usam `@ManyToMany` e a tabela `livro_autor`.
- `Emprestimo` pertence a um `Usuario`.
- `ItemEmprestimo` é uma entidade, e não apenas um `@ManyToMany`, porque o modelo
  lógico lhe atribui um ID próprio.
- `Emprestimo.itens` usa `cascade = ALL` e `orphanRemoval = true`, pois um item não
  existe sem seu empréstimo. Esse cascade não é aplicado a livros, autores ou usuários.
- Associações que apontam para outra entidade usam carregamento `LAZY`. Os dados são
  carregados apenas quando uma consulta realmente precisa deles.
- `StatusEmprestimo` usa `EnumType.STRING`, deixando valores legíveis no banco e
  evitando que a ordem das constantes altere dados existentes.
- `BigDecimal` representa multas sem os erros de arredondamento do tipo `double`.

### `repository`

Os repositories são interfaces que estendem `JpaRepository`. O Spring cria suas
implementações em tempo de execução, por isso não existe uma classe com SQL manual
para cada entidade.

Consultas simples são derivadas do nome do método. A contagem por categoria usa JPQL
porque possui `left join`, agrupamento, contagem e ordenação. A projeção retorna apenas
as duas colunas necessárias.

`@EntityGraph` é usado nos empréstimos para trazer usuário, itens e livros na mesma
consulta. Isso evita uma sequência de consultas adicionais ao percorrer o resultado.
As consultas de livros também carregam categoria e autores para que os DTOs possam ser
montados sem depender de uma sessão JPA aberta durante a serialização.

## Relação entre exercícios e métodos

| Exercício | Repository |
|---|---|
| Livros disponíveis | `findByQuantidadeDisponivelGreaterThanOrderByTituloAsc` |
| Livros por categoria | `findByCategoriaNomeIgnoreCaseOrderByTituloAsc` |
| Usuários por parte do nome | `findByNomeContainingIgnoreCaseOrderByNomeAsc` |
| Empréstimos ativos do usuário | `findDistinctByUsuarioIdAndStatusOrderByDataEmprestimoDesc` |
| Livros de um autor | `findDistinctByAutoresNomeIgnoreCaseOrderByAnoPublicacaoAsc` |
| Empréstimos atrasados | `findDistinctByDataDevolucaoPrevistaBeforeAndStatusOrderByDataDevolucaoPrevistaAsc` |
| Quantidade por categoria | `contarLivrosPorCategoria` |

## Injeção de dependência e baixo acoplamento

Os contratos ficam em `service`, enquanto as classes concretas ficam em
`service.impl`. Cada implementação recebe um repository por construtor:

```java
private final LivroRepository livroRepository;

public LivroConsultaServiceImpl(LivroRepository livroRepository) {
    this.livroRepository = livroRepository;
}
```

A classe não cria a dependência com `new` e não usa injeção em campo. Assim, o
repository pode ser substituído por um mock em teste e a dependência obrigatória fica
explícita e imutável.

O cálculo da data atual segue a mesma regra. `EmprestimoConsultaServiceImpl` recebe um
`Clock`; a aplicação fornece o relógio real e o teste fornece um relógio fixo. Desse
modo, a regra de atraso é determinística.

## Banco H2

O banco principal é H2 em memória. O Hibernate cria ou atualiza as tabelas ao iniciar a
aplicação, e os dados deixam de existir quando o processo termina. Os testes usam outro
banco H2 e recriam o esquema para manter os cenários isolados.

## Testar manualmente pelo Swagger

Inicie a aplicação com o perfil `dev` para carregar um cenário completo de demonstração:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Depois, acesse:

```text
http://localhost:8080/swagger-ui.html
```

O perfil `dev` cria três categorias, três livros, dois autores, dois usuários e três
empréstimos. As datas são calculadas a partir do relógio atual, por isso sempre haverá
um empréstimo atrasado e outro com vencimento futuro.

Rotas disponíveis:

| Método | Rota | Exemplo de parâmetro |
|---|---|---|
| GET | `/api/livros/disponiveis` | Sem parâmetro |
| GET | `/api/livros/por-categoria` | `nome=Tecnologia` |
| GET | `/api/livros/por-autor` | `nome=Ana Código` |
| GET | `/api/usuarios/por-nome` | `trecho=Silva` |
| GET | `/api/usuarios/{id}/emprestimos/ativos` | Use o ID retornado pela busca de usuário |
| GET | `/api/emprestimos/atrasados` | Sem parâmetro |
| GET | `/api/categorias/quantidade-livros` | Sem parâmetro |

Os controllers retornam DTOs, e não entidades JPA. Isso mantém o formato JSON separado
do banco e evita recursão infinita nos relacionamentos bidirecionais.

## Executar os testes

No Windows:

```powershell
.\mvnw.cmd test
```

Os testes de repository usam o JPA e um banco H2 real em memória. Os testes de service
usam Mockito e verificam a lógica sem iniciar o banco.

## Limites desta etapa

- Consultar empréstimos atrasados não altera automaticamente o status para `ATRASADO`.
- Cadastro, realização de empréstimo, devolução e atualização de estoque ainda não são
  casos de uso implementados.
- A API atual é somente de leitura e não possui autenticação ou paginação.
- O Swagger é uma interface de documentação e teste das rotas, não uma interface final
  para usuários da biblioteca.
