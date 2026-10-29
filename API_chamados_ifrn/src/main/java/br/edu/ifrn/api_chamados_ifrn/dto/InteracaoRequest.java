package br.edu.ifrn.api_chamados_ifrn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InteracaoRequest(
        @NotBlank(message = "O texto da interação é obrigatório.")
        String texto,
        @NotNull(message = "O autor da interação é obrigatório.")
        @Positive(message = "O ID do autor deve ser positivo.")
        Long autorId) {
}
