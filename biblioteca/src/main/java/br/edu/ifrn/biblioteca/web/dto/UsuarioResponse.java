package br.edu.ifrn.biblioteca.web.dto;

import java.time.LocalDate;

public record UsuarioResponse(
        Long id,
        String nome,
        String cpf,
        String email,
        String telefone,
        LocalDate dataCadastro,
        String endereco,
        Boolean ativo
) {
}
