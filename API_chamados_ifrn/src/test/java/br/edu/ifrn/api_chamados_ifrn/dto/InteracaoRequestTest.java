package br.edu.ifrn.api_chamados_ifrn.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class InteracaoRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aceitaTextoPreenchidoEAutorPositivo() {
        InteracaoRequest request = new InteracaoRequest("Vou verificar o equipamento.", 3L);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejeitaTextoAusenteOuEmBranco() {
        InteracaoRequest semTexto = new InteracaoRequest(null, 3L);
        InteracaoRequest textoEmBranco = new InteracaoRequest("   ", 3L);

        assertThat(validator.validate(semTexto))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("texto");
        assertThat(validator.validate(textoEmBranco))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("texto");
    }

    @Test
    void rejeitaAutorAusenteOuComIdNaoPositivo() {
        InteracaoRequest semAutor = new InteracaoRequest("Vou verificar.", null);
        InteracaoRequest autorComIdNaoPositivo = new InteracaoRequest("Vou verificar.", 0L);

        assertThat(validator.validate(semAutor))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("autorId");
        assertThat(validator.validate(autorComIdNaoPositivo))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("autorId");
    }
}
