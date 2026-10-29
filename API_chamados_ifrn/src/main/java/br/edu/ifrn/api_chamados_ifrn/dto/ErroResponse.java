package br.edu.ifrn.api_chamados_ifrn.dto;

import java.time.Instant;

public record ErroResponse(Instant timestamp, int status, String mensagem, String caminho) {
}
