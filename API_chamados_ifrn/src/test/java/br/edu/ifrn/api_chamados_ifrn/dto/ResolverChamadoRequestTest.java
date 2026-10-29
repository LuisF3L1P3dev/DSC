package br.edu.ifrn.api_chamados_ifrn.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class ResolverChamadoRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aceitaTecnicoPositivoESolucaoPreenchida() {
        ResolverChamadoRequest request = new ResolverChamadoRequest(3L, "Substituição do cabo de rede.");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejeitaTecnicoAusenteOuNaoPositivo() {
        ResolverChamadoRequest semTecnico = new ResolverChamadoRequest(null, "Problema corrigido.");
        ResolverChamadoRequest idNaoPositivo = new ResolverChamadoRequest(0L, "Problema corrigido.");

        assertThat(validator.validate(semTecnico))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("tecnicoId");
        assertThat(validator.validate(idNaoPositivo))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("tecnicoId");
    }

    @Test
    void rejeitaSolucaoAusenteOuEmBranco() {
        ResolverChamadoRequest semSolucao = new ResolverChamadoRequest(3L, null);
        ResolverChamadoRequest solucaoEmBranco = new ResolverChamadoRequest(3L, "   ");

        assertThat(validator.validate(semSolucao))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("solucao");
        assertThat(validator.validate(solucaoEmBranco))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("solucao");
    }
}
