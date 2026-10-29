package br.edu.ifrn.api_chamados_ifrn.dto;

import br.edu.ifrn.api_chamados_ifrn.model.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ter um formato válido.")
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
        String email,

        @NotBlank(message = "A matrícula é obrigatória.")
        @Size(max = 255, message = "A matrícula deve ter no máximo 255 caracteres.")
        String matricula,

        @NotNull(message = "O tipo de usuário é obrigatório.")
        TipoUsuario tipo,

        @Positive(message = "O ID do setor deve ser positivo.")
        Long setorId) {
}
