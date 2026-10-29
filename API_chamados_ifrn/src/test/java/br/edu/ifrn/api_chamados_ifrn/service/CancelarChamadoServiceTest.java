package br.edu.ifrn.api_chamados_ifrn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.ifrn.api_chamados_ifrn.dto.CancelarChamadoRequest;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancelarChamadoServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ChamadoService chamadoService;

    @Test
    void cancelaChamadoAbertoPeloProprioSolicitante() {
        Chamado chamado = chamadoAberto(1L, 1L);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChamadoResponse response = chamadoService.cancelar(1L, new CancelarChamadoRequest(1L));

        assertThat(response.status()).isEqualTo(Status.CANCELADO);
        assertThat(response.solicitanteId()).isEqualTo(1L);
        verify(chamadoRepository).save(chamado);
    }

    @Test
    void rejeitaChamadoInexistente() {
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.cancelar(99L, new CancelarChamadoRequest(1L)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Chamado n\u00e3o encontrado: 99");
        verify(chamadoRepository, never()).save(any());
    }

    @Test
    void rejeitaCancelamentoPorOutroSolicitante() {
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamadoAberto(1L, 1L)));

        assertThatThrownBy(() -> chamadoService.cancelar(1L, new CancelarChamadoRequest(2L)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Somente o solicitante do chamado pode cancel\u00e1-lo.");
        verify(chamadoRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = "ABERTO", mode = EnumSource.Mode.EXCLUDE)
    void rejeitaChamadoQueNaoEstaAberto(Status status) {
        Chamado chamado = chamadoAberto(1L, 1L);
        chamado.setStatus(status);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));

        assertThatThrownBy(() -> chamadoService.cancelar(1L, new CancelarChamadoRequest(1L)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Somente chamados com status ABERTO podem ser cancelados.");
        verify(chamadoRepository, never()).save(any());
    }

    private static Chamado chamadoAberto(Long chamadoId, Long solicitanteId) {
        Setor setor = new Setor();
        setor.setId(10L);
        setor.setNome("COTIC");
        setor.setSigla("COTIC");

        Categoria categoria = new Categoria();
        categoria.setId(5L);
        categoria.setNome("Rede");
        categoria.setSetor(setor);

        Usuario solicitante = new Usuario();
        solicitante.setId(solicitanteId);
        solicitante.setTipo(TipoUsuario.SOLICITANTE);

        Chamado chamado = new Chamado();
        chamado.setId(chamadoId);
        chamado.setTitulo("Falha na rede");
        chamado.setDescricao("Sem conex\u00e3o");
        chamado.setLocal("Bloco A");
        chamado.setPrioridade(Prioridade.MEDIA);
        chamado.setStatus(Status.ABERTO);
        chamado.setDataAbertura(LocalDateTime.of(2026, 1, 1, 9, 0));
        chamado.setSolicitante(solicitante);
        chamado.setCategoria(categoria);
        return chamado;
    }
}
