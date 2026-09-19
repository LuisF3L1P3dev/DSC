package br.edu.ifrn.biblioteca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifrn.biblioteca.model.Livro;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    @EntityGraph(attributePaths = {"categoria", "autores"})
    List<Livro> findByQuantidadeDisponivelGreaterThanOrderByTituloAsc(Integer quantidade);

    @EntityGraph(attributePaths = {"categoria", "autores"})
    List<Livro> findByCategoriaNomeIgnoreCaseOrderByTituloAsc(String nomeCategoria);

    @EntityGraph(attributePaths = {"categoria", "autores"})
    List<Livro> findDistinctByAutoresNomeIgnoreCaseOrderByAnoPublicacaoAsc(String nomeAutor);
}
