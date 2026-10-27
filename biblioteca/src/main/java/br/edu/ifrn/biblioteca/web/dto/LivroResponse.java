package br.edu.ifrn.biblioteca.web.dto;

import java.util.List;

public record LivroResponse(
        Long id,
        String isbn,
        String titulo,
        String editora,
        Integer anoPublicacao,
        Integer numeroPaginas,
        Integer quantidadeTotal,
        Integer quantidadeDisponivel,
        CategoriaResumoResponse categoria,
        List<AutorResumoResponse> autores
) {
}
