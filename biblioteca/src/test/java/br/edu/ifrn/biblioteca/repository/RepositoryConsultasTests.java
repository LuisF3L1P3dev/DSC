package br.edu.ifrn.biblioteca.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import br.edu.ifrn.biblioteca.model.Autor;
import br.edu.ifrn.biblioteca.model.Categoria;
import br.edu.ifrn.biblioteca.model.Emprestimo;
import br.edu.ifrn.biblioteca.model.Livro;
import br.edu.ifrn.biblioteca.model.StatusEmprestimo;
import br.edu.ifrn.biblioteca.model.Usuario;
import br.edu.ifrn.biblioteca.repository.projection.CategoriaQuantidadeProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryConsultasTests {

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private EntityManager entityManager;

    private Long usuarioJoaoId;
    private Long emprestimoAtrasadoId;

    @BeforeEach
    void prepararDados() {
        Categoria tecnologia = criarCategoria("Tecnologia");
        Categoria literatura = criarCategoria("Literatura");
        criarCategoria("Sem livros");

        Autor autorJava = criarAutor("Ana Código");
        Autor autorLiteratura = criarAutor("Carlos Letras");

        Livro arquitetura = criarLivro(
                "9780000000001", "Arquitetura Limpa", 2017, 2, tecnologia, autorJava
        );
        Livro java = criarLivro(
                "9780000000002", "Java Essencial", 2021, 0, tecnologia, autorJava
        );
        Livro romance = criarLivro(
                "9780000000003", "Romance Brasileiro", 2005, 1, literatura, autorLiteratura
        );
        livroRepository.saveAll(List.of(arquitetura, java, romance));

        Usuario joao = criarUsuario("João Silva", "111.111.111-11", "joao@teste.com");
        Usuario maria = criarUsuario("Maria Sousa", "222.222.222-22", "maria@teste.com");
        usuarioRepository.saveAll(List.of(joao, maria));
        usuarioJoaoId = joao.getId();

        Emprestimo atrasado = criarEmprestimo(
                joao, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 10), StatusEmprestimo.ATIVO,
                arquitetura
        );
        Emprestimo futuro = criarEmprestimo(
                joao, LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 25), StatusEmprestimo.ATIVO,
                romance
        );
        Emprestimo devolvido = criarEmprestimo(
                maria, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 10), StatusEmprestimo.DEVOLVIDO,
                java
        );
        emprestimoRepository.saveAll(List.of(atrasado, futuro, devolvido));
        emprestimoAtrasadoId = atrasado.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void deveListarSomenteLivrosDisponiveisOrdenadosPorTitulo() {
        List<Livro> encontrados = livroRepository
                .findByQuantidadeDisponivelGreaterThanOrderByTituloAsc(0);

        assertThat(encontrados)
                .extracting(Livro::getTitulo)
                .containsExactly("Arquitetura Limpa", "Romance Brasileiro");
    }

    @Test
    void deveBuscarLivrosPorCategoriaIgnorandoMaiusculas() {
        List<Livro> encontrados = livroRepository
                .findByCategoriaNomeIgnoreCaseOrderByTituloAsc("teCNOlogia");

        assertThat(encontrados)
                .extracting(Livro::getTitulo)
                .containsExactly("Arquitetura Limpa", "Java Essencial");
    }

    @Test
    void deveBuscarUsuariosPorParteDoNome() {
        List<Usuario> encontrados = usuarioRepository
                .findByNomeContainingIgnoreCaseOrderByNomeAsc("sILvA");

        assertThat(encontrados)
                .extracting(Usuario::getNome)
                .containsExactly("João Silva");
    }

    @Test
    void deveListarEmprestimosAtivosDoUsuarioComOsLivrosCarregados() {
        List<Emprestimo> encontrados = emprestimoRepository
                .findDistinctByUsuarioIdAndStatusOrderByDataEmprestimoDesc(
                        usuarioJoaoId,
                        StatusEmprestimo.ATIVO
                );

        PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(encontrados).hasSize(2);
        assertThat(encontrados.getFirst().getDataEmprestimo())
                .isEqualTo(LocalDate.of(2026, 9, 15));
        assertThat(util.isLoaded(encontrados.getFirst(), "itens")).isTrue();
        assertThat(util.isLoaded(encontrados.getFirst().getItens().getFirst(), "livro")).isTrue();
    }

    @Test
    void deveBuscarLivrosDoAutorOrdenadosPeloAno() {
        List<Livro> encontrados = livroRepository
                .findDistinctByAutoresNomeIgnoreCaseOrderByAnoPublicacaoAsc("ana código");

        assertThat(encontrados)
                .extracting(Livro::getAnoPublicacao)
                .containsExactly(2017, 2021);
    }

    @Test
    void deveListarSomenteEmprestimosAtivosComPrazoVencido() {
        List<Emprestimo> encontrados = emprestimoRepository
                .findDistinctByDataDevolucaoPrevistaBeforeAndStatusOrderByDataDevolucaoPrevistaAsc(
                        LocalDate.of(2026, 9, 18),
                        StatusEmprestimo.ATIVO
                );

        PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(encontrados)
                .extracting(Emprestimo::getId)
                .containsExactly(emprestimoAtrasadoId);
        assertThat(util.isLoaded(encontrados.getFirst(), "usuario")).isTrue();
        assertThat(util.isLoaded(encontrados.getFirst(), "itens")).isTrue();
    }

    @Test
    void deveContarLivrosPorCategoriaIncluindoCategoriaVazia() {
        List<CategoriaQuantidadeProjection> contagem = categoriaRepository.contarLivrosPorCategoria();

        assertThat(contagem)
                .extracting(
                        CategoriaQuantidadeProjection::getNomeCategoria,
                        CategoriaQuantidadeProjection::getQuantidadeLivros
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Tecnologia", 2L),
                        org.assertj.core.groups.Tuple.tuple("Literatura", 1L),
                        org.assertj.core.groups.Tuple.tuple("Sem livros", 0L)
                );
    }

    private Categoria criarCategoria(String nome) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setDescricao("Descrição de " + nome);
        return categoriaRepository.save(categoria);
    }

    private Autor criarAutor(String nome) {
        Autor autor = new Autor();
        autor.setNome(nome);
        return autorRepository.save(autor);
    }

    private Livro criarLivro(
            String isbn,
            String titulo,
            int ano,
            int quantidadeDisponivel,
            Categoria categoria,
            Autor autor
    ) {
        Livro livro = new Livro();
        livro.setIsbn(isbn);
        livro.setTitulo(titulo);
        livro.setAnoPublicacao(ano);
        livro.setNumeroPaginas(300);
        livro.setQuantidadeTotal(2);
        livro.setQuantidadeDisponivel(quantidadeDisponivel);
        livro.setCategoria(categoria);
        livro.adicionarAutor(autor);
        return livro;
    }

    private Usuario criarUsuario(String nome, String cpf, String email) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setCpf(cpf);
        usuario.setEmail(email);
        usuario.setDataCadastro(LocalDate.of(2026, 1, 1));
        usuario.setAtivo(true);
        return usuario;
    }

    private Emprestimo criarEmprestimo(
            Usuario usuario,
            LocalDate dataEmprestimo,
            LocalDate dataPrevista,
            StatusEmprestimo status,
            Livro livro
    ) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(dataEmprestimo);
        emprestimo.setDataDevolucaoPrevista(dataPrevista);
        emprestimo.setStatus(status);
        emprestimo.setValorMulta(BigDecimal.ZERO);
        emprestimo.adicionarLivro(livro);
        return emprestimo;
    }
}
