package br.edu.ifrn.biblioteca.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifrn.biblioteca.repository.CategoriaRepository;
import br.edu.ifrn.biblioteca.repository.projection.CategoriaQuantidadeProjection;
import br.edu.ifrn.biblioteca.service.CategoriaQuantidade;

@ExtendWith(MockitoExtension.class)
class CategoriaConsultaServiceImplTests {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private CategoriaQuantidadeProjection projection;

    @Test
    void deveConverterProjecaoDoRepositoryParaTipoDoService() {
        when(projection.getNomeCategoria()).thenReturn("Tecnologia");
        when(projection.getQuantidadeLivros()).thenReturn(3L);
        when(categoriaRepository.contarLivrosPorCategoria()).thenReturn(List.of(projection));
        CategoriaConsultaServiceImpl service = new CategoriaConsultaServiceImpl(categoriaRepository);

        List<CategoriaQuantidade> resultado = service.contarLivros();

        assertThat(resultado)
                .containsExactly(new CategoriaQuantidade("Tecnologia", 3L));
    }
}
