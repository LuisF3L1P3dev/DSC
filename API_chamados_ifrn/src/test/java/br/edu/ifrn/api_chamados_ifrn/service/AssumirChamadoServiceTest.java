package br.edu.ifrn.api_chamados_ifrn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.ifrn.api_chamados_ifrn.dto.AssumirChamadoRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoResponse;
import br.edu.ifrn.api_chamados_ifrn.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.api_chamados_ifrn.exception.RegraNegocioException;
import br.edu.ifrn.api_chamados_ifrn.model.Categoria;
import br.edu.ifrn.api_chamados_ifrn.model.Chamado;
import br.edu.ifrn.api_chamados_ifrn.model.Prioridade;
import br.edu.ifrn.api_chamados_ifrn.model.Setor;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssumirChamadoServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ChamadoService chamadoService;

    @Test
    void atribuiTecnicoDoSetorEAvancaStatus() {
        Chamado chamado = chamadoAberto(1L, 10L);
        Usuario tecnico = usuario(3L, TipoUsuario.TECNICO, 10L);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(tecnico));
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChamadoResponse response = chamadoService.assumir(1L, new AssumirChamadoRequest(3L));

        assertThat(response.status()).isEqualTo(Status.EM_ATENDIMENTO);
        assertThat(response.tecnicoId()).isEqualTo(3L);
        verify(chamadoRepository).save(chamado);
    }

    @Test
    void rejeitaChamadoInexistente() {
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.assumir(99L, new AssumirChamadoRequest(3L)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Chamado não encontrado: 99");
        verify(usuarioRepository, never()).findById(3L);
        verify(chamadoRepository, never()).save(any());
    }

    @Test
    void rejeitaChamadoQueNaoEstaAberto() {
        Chamado chamado = chamadoAberto(1L, 10L);
        chamado.setStatus(Status.EM_ATENDIMENTO);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));

        assertThatThrownBy(() -> chamadoService.assumir(1L, new AssumirChamadoRequest(3L)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Somente chamados com status ABERTO podem ser assumidos.");
        verify(usuarioRepository, never()).findById(3L);
        verify(chamadoRepository, never()).save(any());
    }

    @Test
    void rejeitaTecnicoInexistente() {
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamadoAberto(1L, 10L)));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.assumir(1L, new AssumirChamadoRequest(99L)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado: 99");
        verify(chamadoRepository, never()).save(any());
    }

    @Test
    void rejeitaSolicitanteComoTecnicoResponsavel() {
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamadoAberto(1L, 10L)));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, TipoUsuario.SOLICITANTE, null)));

        assertThatThrownBy(() -> chamadoService.assumir(1L, new AssumirChamadoRequest(1L)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O usuário informado não é um técnico.");
        verify(chamadoRepository, never()).save(any());
    }

    @Test
    void rejeitaTecnicoSemSetorOuDeOutroSetor() {
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamadoAberto(1L, 10L)));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, TipoUsuario.TECNICO, null)));
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(usuario(4L, TipoUsuario.TECNICO, 20L)));

        assertThatThrownBy(() -> chamadoService.assumir(1L, new AssumirChamadoRequest(3L)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O técnico deve pertencer ao setor da categoria do chamado.");
        assertThatThrownBy(() -> chamadoService.assumir(1L, new AssumirChamadoRequest(4L)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O técnico deve pertencer ao setor da categoria do chamado.");
        verify(chamadoRepository, never()).save(any());
    }

    private static Chamado chamadoAberto(Long id, Long setorIdCategoria) {
        Setor setor = setor(setorIdCategoria);
        Categoria categoria = new Categoria();
        categoria.setId(5L);
        categoria.setNome("Rede");
        categoria.setSetor(setor);

        Chamado chamado = new Chamado();
        chamado.setId(id);
        chamado.setTitulo("Falha na rede");
        chamado.setDescricao("Sem conexão");
        chamado.setLocal("Bloco A");
        chamado.setPrioridade(Prioridade.MEDIA);
        chamado.setStatus(Status.ABERTO);
        chamado.setDataAbertura(LocalDateTime.of(2026, 1, 1, 9, 0));
        chamado.setSolicitante(usuario(1L, TipoUsuario.SOLICITANTE, null));
        chamado.setCategoria(categoria);
        return chamado;
    }

    private static Usuario usuario(Long id, TipoUsuario tipo, Long setorId) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuário " + id);
        usuario.setEmail("usuario" + id + "@example.com");
        usuario.setMatricula("" + id);
        usuario.setTipo(tipo);
        usuario.setSetor(setorId == null ? null : setor(setorId));
        return usuario;
    }

    private static Setor setor(Long id) {
        Setor setor = new Setor();
        setor.setId(id);
        setor.setNome("Setor " + id);
        setor.setSigla("S" + id);
        return setor;
    }
}
