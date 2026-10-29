package br.edu.ifrn.api_chamados_ifrn.repository;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.ifrn.api_chamados_ifrn.model.TipoUsuario;
import br.edu.ifrn.api_chamados_ifrn.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final PageRequest pagina = PageRequest.of(0, 10, Sort.by("id").ascending());

    @Test
    void pesquisaComCadaCombinacaoDeFiltros() {
        assertThat(usuarioRepository.findAll(pagina).getTotalElements()).isEqualTo(4);

        assertThat(nomes(usuarioRepository.findByNomeContainingIgnoreCase("cArL", pagina)))
                .containsExactly("Carlos");
        assertThat(nomes(usuarioRepository.findByTipo(TipoUsuario.TECNICO, pagina)))
                .containsExactly("Carlos", "Daniela");
        assertThat(nomes(usuarioRepository.findBySetor_Id(1L, pagina))).containsExactly("Carlos");

        assertThat(nomes(usuarioRepository.findByNomeContainingIgnoreCaseAndTipo(
                "c", TipoUsuario.TECNICO, pagina)))
                .containsExactly("Carlos");
        assertThat(nomes(usuarioRepository.findByNomeContainingIgnoreCaseAndSetor_Id(
                "c", 1L, pagina)))
                .containsExactly("Carlos");
        assertThat(nomes(usuarioRepository.findByTipoAndSetor_Id(
                TipoUsuario.TECNICO, 2L, pagina)))
                .containsExactly("Daniela");
        assertThat(nomes(usuarioRepository.findByNomeContainingIgnoreCaseAndTipoAndSetor_Id(
                "c", TipoUsuario.TECNICO, 1L, pagina)))
                .containsExactly("Carlos");
    }

    @Test
    void consultasRetornamMetadadosDePaginacao() {
        Page<Usuario> resultado = usuarioRepository.findByTipo(
                TipoUsuario.TECNICO, PageRequest.of(0, 1, Sort.by("id").ascending()));

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getTotalElements()).isEqualTo(2);
        assertThat(resultado.getTotalPages()).isEqualTo(2);
    }

    private static java.util.List<String> nomes(Page<Usuario> pagina) {
        return pagina.getContent().stream().map(Usuario::getNome).toList();
    }
}
