package br.edu.ifrn.api_chamados_ifrn.dto;

import java.util.List;

public record ChamadoDetalheResponse(ChamadoResponse chamado, List<InteracaoResponse> interacoes) {

    public ChamadoDetalheResponse {
        interacoes = List.copyOf(interacoes);
    }
}
