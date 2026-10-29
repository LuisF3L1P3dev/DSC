package br.edu.ifrn.api_chamados_ifrn.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CancelarChamadoRequest(
        @NotNull(message = "O solicitante é obrigatório.")
        @Positive(message = "O ID do solicitante deve ser positivo.")
        Long solicitanteId) {
}
