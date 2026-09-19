package br.edu.ifrn.biblioteca.web.dto;

public record LivroResumoResponse(
        Long id,
        String isbn,
        String titulo,
        Integer anoPublicacao
) {
}
