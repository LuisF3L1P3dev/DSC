package br.edu.ifrn.api_chamados_ifrn.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CancelarChamadoRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aceitaIdPositivoDoSolicitante() {
        CancelarChamadoRequest request = new CancelarChamadoRequest(1L);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejeitaSolicitanteAusenteOuComIdNaoPositivo() {
        CancelarChamadoRequest semSolicitante = new CancelarChamadoRequest(null);
        CancelarChamadoRequest idNaoPositivo = new CancelarChamadoRequest(0L);

        assertThat(validator.validate(semSolicitante))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("solicitanteId");
        assertThat(validator.validate(idNaoPositivo))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("solicitanteId");
    }
}
