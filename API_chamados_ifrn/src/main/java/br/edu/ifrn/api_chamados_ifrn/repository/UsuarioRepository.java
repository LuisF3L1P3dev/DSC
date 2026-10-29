package br.edu.ifrn.api_chamados_ifrn.repository;

import br.edu.ifrn.api_chamados_ifrn.model.Usuario;
import br.edu.ifrn.api_chamados_ifrn.model.TipoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsBySetor_Id(Long setorId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Usuario> findByTipo(TipoUsuario tipo, Pageable pageable);

    Page<Usuario> findBySetor_Id(Long setorId, Pageable pageable);

    Page<Usuario> findByNomeContainingIgnoreCaseAndTipo(
            String nome, TipoUsuario tipo, Pageable pageable);

    Page<Usuario> findByNomeContainingIgnoreCaseAndSetor_Id(
            String nome, Long setorId, Pageable pageable);

    Page<Usuario> findByTipoAndSetor_Id(
            TipoUsuario tipo, Long setorId, Pageable pageable);

    Page<Usuario> findByNomeContainingIgnoreCaseAndTipoAndSetor_Id(
            String nome, TipoUsuario tipo, Long setorId, Pageable pageable);
}
