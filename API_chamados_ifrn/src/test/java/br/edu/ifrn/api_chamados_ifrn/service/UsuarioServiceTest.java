package br.edu.ifrn.api_chamados_ifrn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.ifrn.api_chamados_ifrn.dto.UsuarioRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.UsuarioResponse;
import br.edu.ifrn.api_chamados_ifrn.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.api_chamados_ifrn.exception.RegraNegocioException;
import br.edu.ifrn.api_chamados_ifrn.model.Setor;
import br.edu.ifrn.api_chamados_ifrn.model.TipoUsuario;
import br.edu.ifrn.api_chamados_ifrn.model.Usuario;
import br.edu.ifrn.api_chamados_ifrn.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SetorService setorService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void cadastraSolicitanteSemSetor() {
        UsuarioRequest request = solicitante("ana@ifrn.edu.br");
        when(usuarioRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(this::salvarComId);

        UsuarioResponse response = usuarioService.cadastrar(request);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.nome()).isEqualTo("Ana Silva");
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.tipo()).isEqualTo(TipoUsuario.SOLICITANTE);
        assertThat(response.setorId()).isNull();
        verify(setorService, never()).buscarPorId(any());
    }

    @Test
    void cadastraTecnicoVinculadoAoSetorInformado() {
        UsuarioRequest request = new UsuarioRequest(
                "Carlos Lima", "carlos@ifrn.edu.br", "2026002", TipoUsuario.TECNICO, 1L);
        Setor setor = new Setor();
        setor.setId(1L);
        when(setorService.buscarPorId(1L)).thenReturn(setor);
        when(usuarioRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(this::salvarComId);

        UsuarioResponse response = usuarioService.cadastrar(request);

        assertThat(response.tipo()).isEqualTo(TipoUsuario.TECNICO);
        assertThat(response.setorId()).isEqualTo(1L);
    }

    @Test
    void rejeitaTecnicoSemSetorComErroDeDadosInvalidos() {
        UsuarioRequest request = new UsuarioRequest(
                "Carlos Lima", "carlos@ifrn.edu.br", "2026002", TipoUsuario.TECNICO, null);

        assertThatThrownBy(() -> usuarioService.cadastrar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Técnicos devem estar vinculados a um setor.");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rejeitaEmailJaCadastrado() {
        UsuarioRequest request = solicitante("ana@example.com");
        when(usuarioRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe um usuário cadastrado com este e-mail.");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void permiteManterEmailDoProprioUsuarioAoAtualizar() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setNome("Ana Silva");
        usuario.setEmail("ana@ifrn.edu.br");
        usuario.setMatricula("2026001");
        usuario.setTipo(TipoUsuario.SOLICITANTE);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("ana@ifrn.edu.br", 7L)).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse response = usuarioService.atualizar(7L, solicitante("ana@ifrn.edu.br"));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.email()).isEqualTo("ana@ifrn.edu.br");
    }

    @Test
    void rejeitaEmailDeOutroUsuarioAoAtualizar() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("bruno@example.com", 7L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.atualizar(7L, solicitante("bruno@example.com")))
                .isInstanceOf(RegraNegocioException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void listaSemFiltrosUsandoConsultaPaginadaPadrao() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(usuarioRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        assertThat(usuarioService.listar("  ", null, null, pageable).getContent()).isEmpty();

        verify(usuarioRepository).findAll(pageable);
    }

    @Test
    void encaminhaFiltrosCombinadosAoRepositorio() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(usuarioRepository.findByNomeContainingIgnoreCaseAndTipoAndSetor_Id(
                "Ana", TipoUsuario.TECNICO, 1L, pageable)).thenReturn(Page.empty(pageable));

        usuarioService.listar(" Ana ", TipoUsuario.TECNICO, 1L, pageable);

        verify(usuarioRepository).findByNomeContainingIgnoreCaseAndTipoAndSetor_Id(
                "Ana", TipoUsuario.TECNICO, 1L, pageable);
    }

    @Test
    void retorna404QuandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(999L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado: 999");
    }

    private UsuarioRequest solicitante(String email) {
        return new UsuarioRequest("Ana Silva", email, "2026001", TipoUsuario.SOLICITANTE, null);
    }

    private Usuario salvarComId(InvocationOnMock invocation) {
        Usuario usuario = invocation.getArgument(0);
        usuario.setId(20L);
        return usuario;
    }
}
