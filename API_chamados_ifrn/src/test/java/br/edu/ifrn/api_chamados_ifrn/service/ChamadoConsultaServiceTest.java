package br.edu.ifrn.api_chamados_ifrn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoDetalheResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoFiltro;
import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.InteracaoResponse;
import br.edu.ifrn.api_chamados_ifrn.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.api_chamados_ifrn.model.Categoria;
import br.edu.ifrn.api_chamados_ifrn.model.Chamado;
import br.edu.ifrn.api_chamados_ifrn.model.Interacao;
import br.edu.ifrn.api_chamados_ifrn.model.Prioridade;
import br.edu.ifrn.api_chamados_ifrn.model.Status;
import br.edu.ifrn.api_chamados_ifrn.model.TipoUsuario;
import br.edu.ifrn.api_chamados_ifrn.model.Usuario;
import br.edu.ifrn.api_chamados_ifrn.repository.CategoriaRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.ChamadoRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ChamadoConsultaServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ChamadoService chamadoService;

    @Test
    void encaminhaFiltrosEMapeiaPaginaDeChamadosParaDtos() {
        PageRequest pageable = PageRequest.of(0, 10);
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 31, 23, 59);
        ChamadoFiltro filtro = new ChamadoFiltro(
                Status.ABERTO, Prioridade.ALTA, 2L, 3L, 1L, null, inicio, fim);
        Chamado chamado = chamado(8L, 1L, TipoUsuario.SOLICITANTE, 3L, null, Status.ABERTO);
        when(chamadoRepository.buscarComFiltros(
                Status.ABERTO, Prioridade.ALTA, 2L, 3L, 1L, null, inicio, fim, pageable))
                .thenReturn(new PageImpl<>(List.of(chamado), pageable, 1));

        Page<ChamadoResponse> resultado = chamadoService.listar(filtro, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(8L);
            assertThat(item.solicitanteId()).isEqualTo(1L);
            assertThat(item.categoriaId()).isEqualTo(3L);
        });
        verify(chamadoRepository).buscarComFiltros(
                Status.ABERTO, Prioridade.ALTA, 2L, 3L, 1L, null, inicio, fim, pageable);
    }

    @Test
    void rejeitaPeriodoComDataInicialDepoisDaFinal() {
        ChamadoFiltro filtro = new ChamadoFiltro(
                null, null, null, null, null, null,
                LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 1, 1, 0, 0));

        assertThatThrownBy(() -> chamadoService.listar(filtro, PageRequest.of(0, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A dataInicio deve ser anterior ou igual à dataFim.");
    }

    @Test
    void retornaDetalheComInteracoesEmOrdemCronologica() {
        Chamado chamado = chamado(8L, 1L, TipoUsuario.SOLICITANTE, 3L, null, Status.ABERTO);
        Interacao recente = interacao(12L, "Técnico respondeu", LocalDateTime.of(2026, 1, 2, 10, 0), 3L);
        Interacao antiga = interacao(11L, "Solicitante abriu o chamado", LocalDateTime.of(2026, 1, 1, 10, 0), 1L);
        chamado.setInteracoes(List.of(recente, antiga));
        when(chamadoRepository.findById(8L)).thenReturn(Optional.of(chamado));

        ChamadoDetalheResponse detalhe = chamadoService.buscarPorId(8L);

        assertThat(detalhe.chamado().id()).isEqualTo(8L);
        assertThat(detalhe.interacoes()).extracting(InteracaoResponse::texto)
                .containsExactly("Solicitante abriu o chamado", "Técnico respondeu");
        assertThat(detalhe.interacoes()).extracting(InteracaoResponse::autorId).containsExactly(1L, 3L);
    }

    @Test
    void listaChamadosAbertosPorSolicitanteComStatus() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, TipoUsuario.SOLICITANTE)));
        when(chamadoRepository.findBySolicitante_IdAndStatus(1L, Status.ABERTO, pageable))
                .thenReturn(Page.empty(pageable));

        Page<ChamadoResponse> resultado = chamadoService.listarPorUsuario(1L, Status.ABERTO, pageable);

        assertThat(resultado.getContent()).isEmpty();
        verify(chamadoRepository).findBySolicitante_IdAndStatus(1L, Status.ABERTO, pageable);
    }

    @Test
    void listaChamadosAtribuidosAoTecnicoSemFiltroDeStatus() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, TipoUsuario.TECNICO)));
        when(chamadoRepository.findByTecnico_Id(3L, pageable)).thenReturn(Page.empty(pageable));

        Page<ChamadoResponse> resultado = chamadoService.listarPorUsuario(3L, null, pageable);

        assertThat(resultado.getContent()).isEmpty();
        verify(chamadoRepository).findByTecnico_Id(3L, pageable);
    }

    @Test
    void retorna404SeUsuarioOuChamadoNaoExistir() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.listarPorUsuario(99L, null, PageRequest.of(0, 10)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> chamadoService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private static Chamado chamado(Long id, Long solicitanteId, TipoUsuario tipoSolicitante,
            Long categoriaId, Long tecnicoId, Status status) {
        Chamado chamado = new Chamado();
        chamado.setId(id);
        chamado.setTitulo("Falha na rede");
        chamado.setDescricao("Sem conexão");
        chamado.setLocal("Bloco A");
        chamado.setPrioridade(Prioridade.ALTA);
        chamado.setStatus(status);
        chamado.setDataAbertura(LocalDateTime.of(2026, 1, 1, 9, 0));
        chamado.setSolicitante(usuario(solicitanteId, tipoSolicitante));
        chamado.setCategoria(categoria(categoriaId));
        chamado.setTecnico(tecnicoId == null ? null : usuario(tecnicoId, TipoUsuario.TECNICO));
        return chamado;
    }

    private static Interacao interacao(Long id, String texto, LocalDateTime dataHora, Long autorId) {
        Interacao interacao = new Interacao();
        interacao.setId(id);
        interacao.setTexto(texto);
        interacao.setDataHora(dataHora);
        interacao.setAutor(usuario(autorId, autorId.equals(1L) ? TipoUsuario.SOLICITANTE : TipoUsuario.TECNICO));
        return interacao;
    }

    private static Usuario usuario(Long id, TipoUsuario tipo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuário " + id);
        usuario.setEmail("usuario" + id + "@example.com");
        usuario.setMatricula("" + id);
        usuario.setTipo(tipo);
        return usuario;
    }

    private static Categoria categoria(Long id) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome("Categoria " + id);
        return categoria;
    }
}
