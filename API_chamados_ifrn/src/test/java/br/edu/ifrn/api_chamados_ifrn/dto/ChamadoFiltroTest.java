package br.edu.ifrn.api_chamados_ifrn.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ChamadoFiltroTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void permiteTodosOsFiltrosOpcionaisAusentes() {
        ChamadoFiltro filtro = new ChamadoFiltro(null, null, null, null, null, null, null, null);

        assertThat(validator.validate(filtro)).isEmpty();
    }

    @Test
    void rejeitaIdsDeFiltroNaoPositivos() {
        ChamadoFiltro filtro = new ChamadoFiltro(null, null, 0L, -1L, null, null, null, null);

        Set<String> camposInvalidos = validator.validate(filtro).stream()
                .map(violacao -> violacao.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(camposInvalidos).contains("setorId", "categoriaId");
    }
}
