package br.edu.ifrn.api_chamados_ifrn.service;

import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoDetalheResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoFiltro;
import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoResponse;
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
import br.edu.ifrn.api_chamados_ifrn.repository.UsuarioRepository;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    public ChamadoService(ChamadoRepository chamadoRepository, UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository) {
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public ChamadoResponse abrir(ChamadoRequest request) {
        Usuario solicitante = usuarioRepository.findById(request.solicitanteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado: " + request.solicitanteId()));
        if (solicitante.getTipo() != TipoUsuario.SOLICITANTE) {
            throw new RegraNegocioException("Somente usuários do tipo SOLICITANTE podem abrir chamados.");
        }

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada: " + request.categoriaId()));

        Chamado chamado = new Chamado();
        chamado.setTitulo(request.titulo());
        chamado.setDescricao(request.descricao());
        chamado.setLocal(request.local());
        chamado.setPrioridade(request.prioridade() == null ? Prioridade.MEDIA : request.prioridade());
        chamado.setStatus(Status.ABERTO);
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setSolicitante(solicitante);
        chamado.setCategoria(categoria);

        return resposta(chamadoRepository.save(chamado));
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listar(ChamadoFiltro filtro, Pageable pageable) {
        validarPeriodo(filtro.dataInicio(), filtro.dataFim());
        Page<Chamado> chamados = chamadoRepository.buscarComFiltros(
                filtro.status(),
                filtro.prioridade(),
                filtro.setorId(),
                filtro.categoriaId(),
                filtro.solicitanteId(),
                filtro.tecnicoId(),
                filtro.dataInicio(),
                filtro.dataFim(),
                pageable);
        return chamados.map(this::resposta);
    }

    @Transactional(readOnly = true)
    public ChamadoDetalheResponse buscarPorId(Long id) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado: " + id));

        List<InteracaoResponse> interacoes = chamado.getInteracoes().stream()
                .sorted(Comparator.comparing(Interacao::getDataHora).thenComparing(Interacao::getId))
                .map(this::resposta)
                .toList();
        return new ChamadoDetalheResponse(resposta(chamado), interacoes);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listarPorUsuario(Long usuarioId, Status status, Pageable pageable) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + usuarioId));

        Page<Chamado> chamados;
        if (usuario.getTipo() == TipoUsuario.SOLICITANTE) {
            chamados = status == null
                    ? chamadoRepository.findBySolicitante_Id(usuarioId, pageable)
                    : chamadoRepository.findBySolicitante_IdAndStatus(usuarioId, status, pageable);
        } else {
            chamados = status == null
                    ? chamadoRepository.findByTecnico_Id(usuarioId, pageable)
                    : chamadoRepository.findByTecnico_IdAndStatus(usuarioId, status, pageable);
        }
        return chamados.map(this::resposta);
    }

    private void validarPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("A dataInicio deve ser anterior ou igual à dataFim.");
        }
    }

    private InteracaoResponse resposta(Interacao interacao) {
        return new InteracaoResponse(
                interacao.getId(), interacao.getTexto(), interacao.getDataHora(), interacao.getAutor().getId());
    }

    private ChamadoResponse resposta(Chamado chamado) {
        Long tecnicoId = chamado.getTecnico() == null ? null : chamado.getTecnico().getId();
        return new ChamadoResponse(
                chamado.getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getLocal(),
                chamado.getPrioridade(),
                chamado.getStatus(),
                chamado.getDataAbertura(),
                chamado.getDataFechamento(),
                chamado.getSolucao(),
                chamado.getSolicitante().getId(),
                chamado.getCategoria().getId(),
                tecnicoId);
    }
}
