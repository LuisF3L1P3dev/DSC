package br.edu.ifrn.api_chamados_ifrn.dto;

import br.edu.ifrn.api_chamados_ifrn.model.TipoUsuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String matricula,
        TipoUsuario tipo,
        Long setorId) {
}
