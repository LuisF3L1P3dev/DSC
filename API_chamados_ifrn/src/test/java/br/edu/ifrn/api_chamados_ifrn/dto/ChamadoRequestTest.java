package br.edu.ifrn.api_chamados_ifrn.dto;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.ifrn.api_chamados_ifrn.model.Prioridade;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ChamadoRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aceitaAberturaValidaComPrioridadeInformada() {
        ChamadoRequest request = new ChamadoRequest(
                "Falha na rede", "Sem conexão no laboratório", "Bloco A", Prioridade.ALTA, 1L, 1L);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void permiteOmitirPrioridadeParaQueOServiceApliqueOPadrao() {
        ChamadoRequest request = new ChamadoRequest(
                "Falha na rede", "Sem conexão no laboratório", "Bloco A", null, 1L, 1L);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejeitaCamposObrigatoriosVaziosEIdsAusentes() {
        ChamadoRequest request = new ChamadoRequest(" ", "", "", null, null, null);

        Set<String> camposInvalidos = validator.validate(request).stream()
                .map(violacao -> violacao.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(camposInvalidos)
                .contains("titulo", "descricao", "local", "solicitanteId", "categoriaId");
    }

    @Test
    void rejeitaIdsQueNaoSejamPositivos() {
        ChamadoRequest request = new ChamadoRequest(
                "Falha na rede", "Sem conexão no laboratório", "Bloco A", null, 0L, -1L);

        assertThat(validator.validate(request))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("solicitanteId", "categoriaId");
    }

    @Test
    void rejeitaTituloOuLocalAcimaDoLimiteDoBanco() {
        String textoLongo = "x".repeat(256);
        ChamadoRequest request = new ChamadoRequest(
                textoLongo, "Descrição", textoLongo, null, 1L, 1L);

        assertThat(validator.validate(request))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("titulo", "local");
    }
}
