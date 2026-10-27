package br.edu.ifrn.biblioteca.config;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import br.edu.ifrn.biblioteca.model.Autor;
import br.edu.ifrn.biblioteca.model.Categoria;
import br.edu.ifrn.biblioteca.model.Emprestimo;
import br.edu.ifrn.biblioteca.model.Livro;
import br.edu.ifrn.biblioteca.model.StatusEmprestimo;
import br.edu.ifrn.biblioteca.model.Usuario;
import br.edu.ifrn.biblioteca.repository.AutorRepository;
import br.edu.ifrn.biblioteca.repository.CategoriaRepository;
import br.edu.ifrn.biblioteca.repository.EmprestimoRepository;
import br.edu.ifrn.biblioteca.repository.LivroRepository;
import br.edu.ifrn.biblioteca.repository.UsuarioRepository;

@Profile("dev")
@Configuration
public class DevDataConfig {

    @Bean
    CommandLineRunner carregarDadosDeDemonstracao(
            CategoriaRepository categoriaRepository,
            AutorRepository autorRepository,
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository,
            EmprestimoRepository emprestimoRepository,
            Clock clock
    ) {
        return args -> {
            if (categoriaRepository.count() > 0) {
                return;
            }

            LocalDate hoje = LocalDate.now(clock);

            Categoria tecnologia = criarCategoria("Tecnologia", "Livros de computação e tecnologia");
            Categoria literatura = criarCategoria("Literatura", "Romances e obras literárias");
            Categoria semLivros = criarCategoria("Sem livros", "Categoria para testar contagem zero");
            categoriaRepository.saveAll(List.of(tecnologia, literatura, semLivros));

            Autor anaCodigo = criarAutor("Ana Código", "Brasileira");
            Autor carlosLetras = criarAutor("Carlos Letras", "Brasileira");
            autorRepository.saveAll(List.of(anaCodigo, carlosLetras));

            Livro arquitetura = criarLivro(
                    "9780000000001", "Arquitetura Limpa", "Editora Técnica", 2017,
                    350, 3, 2, tecnologia, anaCodigo
            );
            Livro java = criarLivro(
                    "9780000000002", "Java Essencial", "Editora Técnica", 2021,
                    420, 2, 0, tecnologia, anaCodigo
            );
            Livro romance = criarLivro(
                    "9780000000003", "Romance Brasileiro", "Editora Letras", 2005,
                    280, 2, 1, literatura, carlosLetras
            );
            livroRepository.saveAll(List.of(arquitetura, java, romance));

            Usuario joao = criarUsuario(
                    "João Silva", "111.111.111-11", "joao@biblioteca.dev", hoje.minusMonths(6)
            );
            Usuario maria = criarUsuario(
                    "Maria Sousa", "222.222.222-22", "maria@biblioteca.dev", hoje.minusMonths(3)
            );
            usuarioRepository.saveAll(List.of(joao, maria));

            Emprestimo atrasado = criarEmprestimo(
                    joao, hoje.minusDays(20), hoje.minusDays(5), null, StatusEmprestimo.ATIVO, arquitetura
            );
            Emprestimo futuro = criarEmprestimo(
                    joao, hoje.minusDays(1), hoje.plusDays(13), null, StatusEmprestimo.ATIVO, romance
            );
            Emprestimo devolvido = criarEmprestimo(
                    maria, hoje.minusDays(30), hoje.minusDays(20), hoje.minusDays(18),
                    StatusEmprestimo.DEVOLVIDO, java
            );
            emprestimoRepository.saveAll(List.of(atrasado, futuro, devolvido));
        };
    }

    private static Categoria criarCategoria(String nome, String descricao) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setDescricao(descricao);
        return categoria;
    }

    private static Autor criarAutor(String nome, String nacionalidade) {
        Autor autor = new Autor();
        autor.setNome(nome);
        autor.setNacionalidade(nacionalidade);
        return autor;
    }

    private static Livro criarLivro(
            String isbn,
            String titulo,
            String editora,
            int anoPublicacao,
            int numeroPaginas,
            int quantidadeTotal,
            int quantidadeDisponivel,
            Categoria categoria,
            Autor autor
    ) {
        Livro livro = new Livro();
        livro.setIsbn(isbn);
        livro.setTitulo(titulo);
        livro.setEditora(editora);
        livro.setAnoPublicacao(anoPublicacao);
        livro.setNumeroPaginas(numeroPaginas);
        livro.setQuantidadeTotal(quantidadeTotal);
        livro.setQuantidadeDisponivel(quantidadeDisponivel);
        livro.setCategoria(categoria);
        livro.adicionarAutor(autor);
        return livro;
    }

    private static Usuario criarUsuario(String nome, String cpf, String email, LocalDate dataCadastro) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setCpf(cpf);
        usuario.setEmail(email);
        usuario.setTelefone("(84) 99999-0000");
        usuario.setDataCadastro(dataCadastro);
        usuario.setEndereco("Natal/RN");
        usuario.setAtivo(true);
        return usuario;
    }

    private static Emprestimo criarEmprestimo(
            Usuario usuario,
            LocalDate dataEmprestimo,
            LocalDate dataPrevista,
            LocalDate dataEfetiva,
            StatusEmprestimo status,
            Livro livro
    ) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(dataEmprestimo);
        emprestimo.setDataDevolucaoPrevista(dataPrevista);
        emprestimo.setDataDevolucaoEfetiva(dataEfetiva);
        emprestimo.setStatus(status);
        emprestimo.setValorMulta(BigDecimal.ZERO);
        emprestimo.adicionarLivro(livro);
        return emprestimo;
    }
}
