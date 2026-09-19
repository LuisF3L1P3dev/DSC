package br.edu.ifrn.biblioteca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifrn.biblioteca.model.Livro;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    List<Livro> findByQuantidadeDisponivelGreaterThanOrderByTituloAsc(Integer quantidade);

    List<Livro> findByCategoriaNomeIgnoreCaseOrderByTituloAsc(String nomeCategoria);

    List<Livro> findDistinctByAutoresNomeIgnoreCaseOrderByAnoPublicacaoAsc(String nomeAutor);
}
