package br.edu.ifrn.api_chamados_ifrn.service;

import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoDetalheResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoFiltro;
import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.InteracaoResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.AssumirChamadoRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.CancelarChamadoRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.InteracaoRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.ResolverChamadoRequest;
import br.edu.ifrn.api_chamados_ifrn.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.api_chamados_ifrn.exception.RegraNegocioException;
import br.edu.ifrn.api_chamados_ifrn.model.Categoria;
import br.edu.ifrn.api_chamados_ifrn.model.Chamado;
import br.edu.ifrn.api_chamados_ifrn.model.Interacao;
import br.edu.ifrn.api_chamados_ifrn.model.Prioridade;
import br.edu.ifrn.api_chamados_ifrn.model.Setor;
import br.edu.ifrn.api_chamados_ifrn.model.Status;
import br.edu.ifrn.api_chamados_ifrn.model.TipoUsuario;
import br.edu.ifrn.api_chamados_ifrn.model.Usuario;
import br.edu.ifrn.api_chamados_ifrn.repository.CategoriaRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.ChamadoRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.InteracaoRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.UsuarioRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
    private final InteracaoRepository interacaoRepository;

    public ChamadoService(ChamadoRepository chamadoRepository, UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository, InteracaoRepository interacaoRepository) {
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.interacaoRepository = interacaoRepository;
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

    @Transactional
    public ChamadoResponse assumir(Long chamadoId, AssumirChamadoRequest request) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado: " + chamadoId));
        if (chamado.getStatus() != Status.ABERTO) {
            throw new RegraNegocioException("Somente chamados com status ABERTO podem ser assumidos.");
        }

        Usuario tecnico = usuarioRepository.findById(request.tecnicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado: " + request.tecnicoId()));
        if (tecnico.getTipo() != TipoUsuario.TECNICO) {
            throw new RegraNegocioException("O usuário informado não é um técnico.");
        }

        Setor setorTecnico = tecnico.getSetor();
        Setor setorCategoria = chamado.getCategoria().getSetor();
        if (setorTecnico == null || !Objects.equals(setorTecnico.getId(), setorCategoria.getId())) {
            throw new RegraNegocioException("O técnico deve pertencer ao setor da categoria do chamado.");
        }

        chamado.setTecnico(tecnico);
        chamado.setStatus(Status.EM_ATENDIMENTO);
        return resposta(chamadoRepository.save(chamado));
    }

    @Transactional
    public ChamadoResponse resolver(Long chamadoId, ResolverChamadoRequest request) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado n\u00e3o encontrado: " + chamadoId));
        if (chamado.getStatus() != Status.EM_ATENDIMENTO) {
            throw new RegraNegocioException("Somente chamados em atendimento podem ser resolvidos.");
        }
        if (chamado.getTecnico() == null || !Objects.equals(chamado.getTecnico().getId(), request.tecnicoId())) {
            throw new RegraNegocioException("Somente o t\u00e9cnico atribu\u00eddo pode resolver o chamado.");
        }
        if (request.solucao() == null || request.solucao().isBlank()) {
            throw new IllegalArgumentException("A solu\u00e7\u00e3o \u00e9 obrigat\u00f3ria.");
        }

        chamado.setSolucao(request.solucao());
        chamado.setStatus(Status.RESOLVIDO);
        chamado.setDataFechamento(LocalDateTime.now());
        return resposta(chamadoRepository.save(chamado));
    }

    @Transactional
    public ChamadoResponse cancelar(Long chamadoId, CancelarChamadoRequest request) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado n\u00e3o encontrado: " + chamadoId));
        if (chamado.getStatus() != Status.ABERTO) {
            throw new RegraNegocioException("Somente chamados com status ABERTO podem ser cancelados.");
        }
        if (!Objects.equals(chamado.getSolicitante().getId(), request.solicitanteId())) {
            throw new RegraNegocioException("Somente o solicitante do chamado pode cancel\u00e1-lo.");
        }

        chamado.setStatus(Status.CANCELADO);
        return resposta(chamadoRepository.save(chamado));
    }

    @Transactional
    public InteracaoResponse registrarInteracao(Long chamadoId, InteracaoRequest request) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado n\u00e3o encontrado: " + chamadoId));
        if (chamado.getStatus() == Status.RESOLVIDO || chamado.getStatus() == Status.CANCELADO) {
            throw new RegraNegocioException("Chamados resolvidos ou cancelados n\u00e3o aceitam novas intera\u00e7\u00f5es.");
        }

        Usuario autor = usuarioRepository.findById(request.autorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usu\u00e1rio n\u00e3o encontrado: " + request.autorId()));
        boolean autorEhSolicitante = Objects.equals(chamado.getSolicitante().getId(), autor.getId());
        boolean autorEhTecnicoAtribuido = chamado.getTecnico() != null
                && Objects.equals(chamado.getTecnico().getId(), autor.getId());
        if (!autorEhSolicitante && !autorEhTecnicoAtribuido) {
            throw new RegraNegocioException(
                    "Somente o solicitante ou o t\u00e9cnico atribu\u00eddo podem interagir com o chamado.");
        }

        Interacao interacao = new Interacao();
        interacao.setTexto(request.texto());
        interacao.setDataHora(LocalDateTime.now());
        interacao.setChamado(chamado);
        interacao.setAutor(autor);
        return resposta(interacaoRepository.save(interacao));
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
