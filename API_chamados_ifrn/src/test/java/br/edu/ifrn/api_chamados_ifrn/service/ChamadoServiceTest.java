package br.edu.ifrn.api_chamados_ifrn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoResponse;
import br.edu.ifrn.api_chamados_ifrn.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.api_chamados_ifrn.exception.RegraNegocioException;
import br.edu.ifrn.api_chamados_ifrn.model.Categoria;
import br.edu.ifrn.api_chamados_ifrn.model.Chamado;
import br.edu.ifrn.api_chamados_ifrn.model.Prioridade;
import br.edu.ifrn.api_chamados_ifrn.model.Status;
import br.edu.ifrn.api_chamados_ifrn.model.TipoUsuario;
import br.edu.ifrn.api_chamados_ifrn.model.Usuario;
import br.edu.ifrn.api_chamados_ifrn.repository.CategoriaRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.ChamadoRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChamadoServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ChamadoService chamadoService;

    @Test
    void abreChamadoComStatusDataEPrioridadePadrao() {
        Usuario solicitante = usuario(1L, TipoUsuario.SOLICITANTE);
        Categoria categoria = categoria(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(solicitante));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(this::salvarComId);
        LocalDateTime inicio = LocalDateTime.now().minusSeconds(1);

        ChamadoResponse response = chamadoService.abrir(pedido(null));

        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.prioridade()).isEqualTo(Prioridade.MEDIA);
        assertThat(response.status()).isEqualTo(Status.ABERTO);
        assertThat(response.dataAbertura()).isBetween(inicio, LocalDateTime.now().plusSeconds(1));
        assertThat(response.dataFechamento()).isNull();
        assertThat(response.solucao()).isNull();
        assertThat(response.tecnicoId()).isNull();
        assertThat(response.solicitanteId()).isEqualTo(1L);
        assertThat(response.categoriaId()).isEqualTo(1L);
    }

    @Test
    void preservaPrioridadeInformadaPeloSolicitante() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, TipoUsuario.SOLICITANTE)));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria(1L)));
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(this::salvarComId);

        ChamadoResponse response = chamadoService.abrir(pedido(Prioridade.ALTA));

        assertThat(response.prioridade()).isEqualTo(Prioridade.ALTA);
    }

    @Test
    void impedeTecnicoDeAbrirChamado() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, TipoUsuario.TECNICO)));

        assertThatThrownBy(() -> chamadoService.abrir(pedido(null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Somente usuários do tipo SOLICITANTE podem abrir chamados.");
        verify(categoriaRepository, never()).findById(1L);
        verify(chamadoRepository, never()).save(any());
    }

    @Test
    void rejeitaSolicitanteInexistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.abrir(pedido(null)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado: 1");
        verify(chamadoRepository, never()).save(any());
    }

    @Test
    void rejeitaCategoriaInexistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, TipoUsuario.SOLICITANTE)));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.abrir(pedido(null)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Categoria não encontrada: 1");
        verify(chamadoRepository, never()).save(any());
    }

    private static ChamadoRequest pedido(Prioridade prioridade) {
        return new ChamadoRequest(
                "Falha na rede", "Sem conexão no laboratório", "Bloco A", prioridade, 1L, 1L);
    }

    private static Usuario usuario(Long id, TipoUsuario tipo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Ana");
        usuario.setEmail("ana@example.com");
        usuario.setMatricula("1001");
        usuario.setTipo(tipo);
        return usuario;
    }

    private static Categoria categoria(Long id) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome("Rede");
        return categoria;
    }

    private Chamado salvarComId(InvocationOnMock invocation) {
        Chamado chamado = invocation.getArgument(0);
        chamado.setId(50L);
        return chamado;
    }
}
