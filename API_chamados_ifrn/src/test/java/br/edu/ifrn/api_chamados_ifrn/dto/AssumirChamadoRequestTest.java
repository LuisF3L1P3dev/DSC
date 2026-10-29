package br.edu.ifrn.api_chamados_ifrn.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class AssumirChamadoRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aceitaIdDeTecnicoPositivo() {
        AssumirChamadoRequest request = new AssumirChamadoRequest(3L);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejeitaIdDeTecnicoAusenteOuNaoPositivo() {
        AssumirChamadoRequest semId = new AssumirChamadoRequest(null);
        AssumirChamadoRequest idZero = new AssumirChamadoRequest(0L);

        assertThat(validator.validate(semId)).extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("tecnicoId");
        assertThat(validator.validate(idZero)).extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("tecnicoId");
    }
}
