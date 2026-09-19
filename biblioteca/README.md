# Sistema de Biblioteca com Spring Data JPA

Projeto educacional que implementa o modelo lógico de uma biblioteca usando Spring Boot,
Spring Data JPA e H2.

O escopo atual contém as entidades, os repositories, os services de consulta e os testes.
Não há API REST nesta etapa.

## Organização das pastas

```text
src/main/java/br/edu/ifrn/biblioteca
|- config/                 Beans de infraestrutura usados pela aplicação
|- model/                  Entidades JPA e enumerações do domínio
|- repository/             Interfaces que abstraem o acesso ao banco
|  `- projection/          Formatos internos de resultados agregados
|- service/                Contratos dos casos de uso de consulta
|  `- impl/                Implementações dos contratos
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
- Controllers REST, DTOs de API e interface gráfica ficam para uma próxima etapa.
