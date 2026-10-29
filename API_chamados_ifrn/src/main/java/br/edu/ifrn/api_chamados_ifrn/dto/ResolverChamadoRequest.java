package br.edu.ifrn.api_chamados_ifrn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ResolverChamadoRequest(
        @NotNull(message = "O técnico é obrigatório.")
        @Positive(message = "O ID do técnico deve ser positivo.")
        Long tecnicoId,
        @NotBlank(message = "A solução é obrigatória.")
        String solucao) {
}
