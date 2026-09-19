package br.edu.ifrn.biblioteca.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifrn.biblioteca.model.Autor;

public interface AutorRepository extends JpaRepository<Autor, Long> {
}
