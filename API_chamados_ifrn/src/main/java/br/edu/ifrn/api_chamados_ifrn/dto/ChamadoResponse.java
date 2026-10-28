package br.edu.ifrn.api_chamados_ifrn.dto;

import br.edu.ifrn.api_chamados_ifrn.model.Prioridade;
import br.edu.ifrn.api_chamados_ifrn.model.Status;
import java.time.LocalDateTime;

public record ChamadoResponse(
        Long id,
        String titulo,
        String descricao,
        String local,
        Prioridade prioridade,
        Status status,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        String solucao,
        Long solicitanteId,
        Long categoriaId,
        Long tecnicoId) {
}
