package br.edu.ifrn.api_chamados_ifrn.dto;

import br.edu.ifrn.api_chamados_ifrn.model.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ChamadoRequest(
        @NotBlank(message = "O título é obrigatório.")
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres.")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória.")
        String descricao,

        @NotBlank(message = "O local é obrigatório.")
        @Size(max = 255, message = "O local deve ter no máximo 255 caracteres.")
        String local,

        Prioridade prioridade,

        @NotNull(message = "O solicitante é obrigatório.")
        @Positive(message = "O ID do solicitante deve ser positivo.")
        Long solicitanteId,

        @NotNull(message = "A categoria é obrigatória.")
        @Positive(message = "O ID da categoria deve ser positivo.")
        Long categoriaId) {
}
