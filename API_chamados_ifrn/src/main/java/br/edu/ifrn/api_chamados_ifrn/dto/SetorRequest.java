package br.edu.ifrn.api_chamados_ifrn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetorRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres.")
        String nome,

        @NotBlank(message = "A sigla é obrigatória.")
        @Size(max = 255, message = "A sigla deve ter no máximo 255 caracteres.")
        String sigla) {
}
