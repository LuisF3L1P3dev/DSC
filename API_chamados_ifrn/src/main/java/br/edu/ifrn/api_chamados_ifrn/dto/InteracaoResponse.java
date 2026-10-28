package br.edu.ifrn.api_chamados_ifrn.dto;

import java.time.LocalDateTime;

public record InteracaoResponse(Long id, String texto, LocalDateTime dataHora, Long autorId) {
}
