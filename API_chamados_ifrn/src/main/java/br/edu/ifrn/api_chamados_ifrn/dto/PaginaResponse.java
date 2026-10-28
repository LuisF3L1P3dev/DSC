package br.edu.ifrn.api_chamados_ifrn.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PaginaResponse<T>(
        List<T> itens,
        int numeroPagina,
        int tamanhoPagina,
        long totalElementos,
        int totalPaginas) {

    public static <T> PaginaResponse<T> de(Page<T> pagina) {
        return new PaginaResponse<>(pagina.getContent(), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }
}
