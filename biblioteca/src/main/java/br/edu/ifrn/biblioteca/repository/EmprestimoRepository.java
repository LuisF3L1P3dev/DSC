package br.edu.ifrn.biblioteca.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifrn.biblioteca.model.Emprestimo;
import br.edu.ifrn.biblioteca.model.StatusEmprestimo;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    @EntityGraph(attributePaths = {"usuario", "itens", "itens.livro"})
    List<Emprestimo> findDistinctByUsuarioIdAndStatusOrderByDataEmprestimoDesc(
            Long usuarioId,
            StatusEmprestimo status
    );

    @EntityGraph(attributePaths = {"usuario", "itens", "itens.livro"})
    List<Emprestimo> findDistinctByDataDevolucaoPrevistaBeforeAndStatusOrderByDataDevolucaoPrevistaAsc(
            LocalDate dataLimite,
            StatusEmprestimo status
    );
}
