package br.edu.ifrn.api_chamados_ifrn.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssumirChamadoRequest(
        @NotNull(message = "O técnico é obrigatório.")
        @Positive(message = "O ID do técnico deve ser positivo.")
        Long tecnicoId) {
}
