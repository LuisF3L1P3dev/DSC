package br.edu.ifrn.api_chamados_ifrn.repository;

import br.edu.ifrn.api_chamados_ifrn.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsBySetor_Id(Long setorId);

    Page<Categoria> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Categoria> findBySetor_Id(Long setorId, Pageable pageable);

    Page<Categoria> findByNomeContainingIgnoreCaseAndSetor_Id(String nome, Long setorId, Pageable pageable);
}
