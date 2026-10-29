package br.edu.ifrn.api_chamados_ifrn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.ifrn.api_chamados_ifrn.dto.InteracaoRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.InteracaoResponse;
import br.edu.ifrn.api_chamados_ifrn.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.api_chamados_ifrn.exception.RegraNegocioException;
import br.edu.ifrn.api_chamados_ifrn.model.Categoria;
import br.edu.ifrn.api_chamados_ifrn.model.Chamado;
import br.edu.ifrn.api_chamados_ifrn.model.Interacao;
import br.edu.ifrn.api_chamados_ifrn.model.Prioridade;
import br.edu.ifrn.api_chamados_ifrn.model.Status;
import br.edu.ifrn.api_chamados_ifrn.model.TipoUsuario;
import br.edu.ifrn.api_chamados_ifrn.model.Usuario;
import br.edu.ifrn.api_chamados_ifrn.repository.CategoriaRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.ChamadoRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.InteracaoRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarInteracaoServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private InteracaoRepository interacaoRepository;

    @InjectMocks
    private ChamadoService chamadoService;

    @Test
    void permiteSolicitanteRegistrarInteracaoEmChamadoAberto() {
        Chamado chamado = chamado(1L, Status.ABERTO, 1L, null);
        Usuario autor = usuario(1L, TipoUsuario.SOLICITANTE);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(interacaoRepository.save(any(Interacao.class))).thenAnswer(invocation -> {
            Interacao interacao = invocation.getArgument(0);
            interacao.setId(50L);
            return interacao;
        });
        LocalDateTime antesDoRegistro = LocalDateTime.now().minusSeconds(1);

        InteracaoResponse response = chamadoService.registrarInteracao(
                1L, new InteracaoRequest("Ainda estou sem conex\u00e3o.", 1L));

        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.texto()).isEqualTo("Ainda estou sem conex\u00e3o.");
        assertThat(response.autorId()).isEqualTo(1L);
        assertThat(response.dataHora()).isBetween(antesDoRegistro, LocalDateTime.now().plusSeconds(1));
        verify(interacaoRepository).save(any(Interacao.class));
    }

    @Test
    void permiteTecnicoAtribuidoRegistrarInteracaoDuranteAtendimento() {
        Chamado chamado = chamado(1L, Status.EM_ATENDIMENTO, 1L, 3L);
        Usuario autor = usuario(3L, TipoUsuario.TECNICO);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(autor));
        when(interacaoRepository.save(any(Interacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InteracaoResponse response = chamadoService.registrarInteracao(
                1L, new InteracaoRequest("Estou verificando o equipamento.", 3L));

        assertThat(response.autorId()).isEqualTo(3L);
        assertThat(response.texto()).isEqualTo("Estou verificando o equipamento.");
        verify(interacaoRepository).save(any(Interacao.class));
    }

    @Test
    void rejeitaChamadoInexistente() {
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.registrarInteracao(
                99L, new InteracaoRequest("Mensagem", 1L)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Chamado n\u00e3o encontrado: 99");
        verify(usuarioRepository, never()).findById(1L);
        verify(interacaoRepository, never()).save(any());
    }

    @Test
    void rejeitaAutorInexistente() {
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado(1L, Status.ABERTO, 1L, null)));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.registrarInteracao(
                1L, new InteracaoRequest("Mensagem", 99L)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usu\u00e1rio n\u00e3o encontrado: 99");
        verify(interacaoRepository, never()).save(any());
    }

    @Test
    void rejeitaUsuarioQueNaoEParticipanteDoChamado() {
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado(1L, Status.ABERTO, 1L, 3L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, TipoUsuario.SOLICITANTE)));

        assertThatThrownBy(() -> chamadoService.registrarInteracao(
                1L, new InteracaoRequest("Mensagem", 2L)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Somente o solicitante ou o t\u00e9cnico atribu\u00eddo podem interagir com o chamado.");
        verify(interacaoRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = {"RESOLVIDO", "CANCELADO"})
    void rejeitaChamadoEncerrado(Status status) {
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado(1L, status, 1L, 3L)));

        assertThatThrownBy(() -> chamadoService.registrarInteracao(
                1L, new InteracaoRequest("Mensagem", 1L)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Chamados resolvidos ou cancelados n\u00e3o aceitam novas intera\u00e7\u00f5es.");
        verify(usuarioRepository, never()).findById(1L);
        verify(interacaoRepository, never()).save(any());
    }

    private static Chamado chamado(Long id, Status status, Long solicitanteId, Long tecnicoId) {
        Categoria categoria = new Categoria();
        categoria.setId(5L);
        categoria.setNome("Rede");

        Chamado chamado = new Chamado();
        chamado.setId(id);
        chamado.setTitulo("Falha na rede");
        chamado.setDescricao("Sem conex\u00e3o");
        chamado.setLocal("Bloco A");
        chamado.setPrioridade(Prioridade.MEDIA);
        chamado.setStatus(status);
        chamado.setDataAbertura(LocalDateTime.of(2026, 1, 1, 9, 0));
        chamado.setSolicitante(usuario(solicitanteId, TipoUsuario.SOLICITANTE));
        chamado.setCategoria(categoria);
        chamado.setTecnico(tecnicoId == null ? null : usuario(tecnicoId, TipoUsuario.TECNICO));
        return chamado;
    }

    private static Usuario usuario(Long id, TipoUsuario tipo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setTipo(tipo);
        return usuario;
    }
}
