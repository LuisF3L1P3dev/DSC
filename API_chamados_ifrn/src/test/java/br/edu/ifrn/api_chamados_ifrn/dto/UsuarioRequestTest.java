package br.edu.ifrn.api_chamados_ifrn.dto;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.ifrn.api_chamados_ifrn.model.TipoUsuario;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class UsuarioRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aceitaSolicitanteSemSetor() {
        UsuarioRequest request = new UsuarioRequest(
                "Ana Silva", "ana@ifrn.edu.br", "2026001", TipoUsuario.SOLICITANTE, null);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void aceitaTecnicoComSetorPositivo() {
        UsuarioRequest request = new UsuarioRequest(
                "Carlos Lima", "carlos@ifrn.edu.br", "2026002", TipoUsuario.TECNICO, 1L);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejeitaCamposObrigatoriosVaziosEEmailInvalido() {
        UsuarioRequest request = new UsuarioRequest(" ", "email-invalido", "", null, null);

        Set<String> camposInvalidos = validator.validate(request).stream()
                .map(violacao -> violacao.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(camposInvalidos).contains("nome", "email", "matricula", "tipo");
        assertThat(camposInvalidos).doesNotContain("setorId");
    }

    @Test
    void rejeitaIdDeSetorZeroOuNegativo() {
        UsuarioRequest request = new UsuarioRequest(
                "Ana Silva", "ana@ifrn.edu.br", "2026001", TipoUsuario.SOLICITANTE, 0L);

        assertThat(validator.validate(request))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("setorId");
    }
}
