package br.edu.ifrn.biblioteca.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.edu.ifrn.biblioteca.model.StatusEmprestimo;

public record EmprestimoResponse(
        Long id,
        UsuarioResumoResponse usuario,
        LocalDate dataEmprestimo,
        LocalDate dataDevolucaoPrevista,
        LocalDate dataDevolucaoEfetiva,
        StatusEmprestimo status,
        BigDecimal valorMulta,
        List<LivroResumoResponse> livros
) {
}
