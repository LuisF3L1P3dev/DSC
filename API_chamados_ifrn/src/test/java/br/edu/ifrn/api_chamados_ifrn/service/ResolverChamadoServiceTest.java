package br.edu.ifrn.api_chamados_ifrn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.ResolverChamadoRequest;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolverChamadoServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ChamadoService chamadoService;

    @Test
    void resolveChamadoPeloTecnicoAtribuidoEPreencheDadosDeFechamento() {
        Chamado chamado = chamadoEmAtendimento(1L, 3L);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LocalDateTime antesDaResolucao = LocalDateTime.now().minusSeconds(1);

        ChamadoResponse response = chamadoService.resolver(
                1L, new ResolverChamadoRequest(3L, "Cabo de rede substitu\u00eddo."));

        assertThat(response.status()).isEqualTo(Status.RESOLVIDO);
        assertThat(response.solucao()).isEqualTo("Cabo de rede substitu\u00eddo.");
        assertThat(response.dataFechamento()).isBetween(antesDaResolucao, LocalDateTime.now().plusSeconds(1));
        assertThat(response.tecnicoId()).isEqualTo(3L);
        verify(chamadoRepository).save(chamado);
    }

    @Test
    void rejeitaChamadoInexistente() {
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.resolver(
                99L, new ResolverChamadoRequest(3L, "Problema corrigido.")))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Chamado n\u00e3o encontrado: 99");
        verify(chamadoRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = "EM_ATENDIMENTO", mode = EnumSource.Mode.EXCLUDE)
    void rejeitaChamadoForaDoAtendimento(Status status) {
        Chamado chamado = chamadoEmAtendimento(1L, 3L);
        chamado.setStatus(status);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));

        assertThatThrownBy(() -> chamadoService.resolver(
                1L, new ResolverChamadoRequest(3L, "Problema corrigido.")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Somente chamados em atendimento podem ser resolvidos.");
        verify(chamadoRepository, never()).save(any());
    }

    @Test
    void rejeitaTecnicoDiferenteDoAtribuido() {
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamadoEmAtendimento(1L, 3L)));

        assertThatThrownBy(() -> chamadoService.resolver(
                1L, new ResolverChamadoRequest(4L, "Problema corrigido.")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Somente o t\u00e9cnico atribu\u00eddo pode resolver o chamado.");
        verify(chamadoRepository, never()).save(any());
    }

    @Test
    void rejeitaChamadoSemTecnicoAtribuido() {
        Chamado chamado = chamadoEmAtendimento(1L, 3L);
        chamado.setTecnico(null);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));

        assertThatThrownBy(() -> chamadoService.resolver(
                1L, new ResolverChamadoRequest(3L, "Problema corrigido.")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Somente o t\u00e9cnico atribu\u00eddo pode resolver o chamado.");
        verify(chamadoRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejeitaSolucaoAusenteOuEmBranco(String solucao) {
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamadoEmAtendimento(1L, 3L)));

        assertThatThrownBy(() -> chamadoService.resolver(1L, new ResolverChamadoRequest(3L, solucao)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A solu\u00e7\u00e3o \u00e9 obrigat\u00f3ria.");
        verify(chamadoRepository, never()).save(any());
    }

    private static Chamado chamadoEmAtendimento(Long chamadoId, Long tecnicoId) {
        Setor setor = new Setor();
        setor.setId(10L);
        setor.setNome("COTIC");
        setor.setSigla("COTIC");

        Categoria categoria = new Categoria();
        categoria.setId(5L);
        categoria.setNome("Rede");
        categoria.setSetor(setor);

        Usuario solicitante = new Usuario();
        solicitante.setId(1L);
        solicitante.setTipo(TipoUsuario.SOLICITANTE);

        Usuario tecnico = new Usuario();
        tecnico.setId(tecnicoId);
        tecnico.setTipo(TipoUsuario.TECNICO);
        tecnico.setSetor(setor);

        Chamado chamado = new Chamado();
        chamado.setId(chamadoId);
        chamado.setTitulo("Falha na rede");
        chamado.setDescricao("Sem conex\u00e3o");
        chamado.setLocal("Bloco A");
        chamado.setPrioridade(Prioridade.MEDIA);
        chamado.setStatus(Status.EM_ATENDIMENTO);
        chamado.setDataAbertura(LocalDateTime.of(2026, 1, 1, 9, 0));
        chamado.setSolicitante(solicitante);
        chamado.setCategoria(categoria);
        chamado.setTecnico(tecnico);
        return chamado;
    }
}
