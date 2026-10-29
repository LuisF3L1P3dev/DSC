package br.edu.ifrn.api_chamados_ifrn.dto;

import br.edu.ifrn.api_chamados_ifrn.model.Prioridade;
import br.edu.ifrn.api_chamados_ifrn.model.Status;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record ChamadoFiltro(
        Status status,
        Prioridade prioridade,

        @Positive(message = "O ID do setor deve ser positivo.")
        Long setorId,

        @Positive(message = "O ID da categoria deve ser positivo.")
        Long categoriaId,

        @Positive(message = "O ID do solicitante deve ser positivo.")
        Long solicitanteId,

        @Positive(message = "O ID do técnico deve ser positivo.")
        Long tecnicoId,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dataInicio,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dataFim) {
}
