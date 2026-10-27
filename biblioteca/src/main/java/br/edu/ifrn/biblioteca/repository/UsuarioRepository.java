package br.edu.ifrn.biblioteca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifrn.biblioteca.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findByNomeContainingIgnoreCaseOrderByNomeAsc(String trechoNome);
}
