package br.edu.ifrn.api_chamados_ifrn.service;

import br.edu.ifrn.api_chamados_ifrn.dto.UsuarioRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.UsuarioResponse;
import br.edu.ifrn.api_chamados_ifrn.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.api_chamados_ifrn.exception.RegraNegocioException;
import br.edu.ifrn.api_chamados_ifrn.model.Setor;
import br.edu.ifrn.api_chamados_ifrn.model.TipoUsuario;
import br.edu.ifrn.api_chamados_ifrn.model.Usuario;
import br.edu.ifrn.api_chamados_ifrn.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SetorService setorService;

    public UsuarioService(UsuarioRepository usuarioRepository, SetorService setorService) {
        this.usuarioRepository = usuarioRepository;
        this.setorService = setorService;
    }

    @Transactional
    public UsuarioResponse cadastrar(UsuarioRequest request) {
        Usuario usuario = new Usuario();
        preencher(usuario, request);
        validarEmailUnico(request.email(), null);
        return resposta(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return resposta(obter(id));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(String nome, TipoUsuario tipo, Long setorId, Pageable pageable) {
        String nomeFiltro = nome == null || nome.isBlank() ? null : nome.trim();
        Page<Usuario> usuarios;

        if (nomeFiltro != null && tipo != null && setorId != null) {
            usuarios = usuarioRepository.findByNomeContainingIgnoreCaseAndTipoAndSetor_Id(
                    nomeFiltro, tipo, setorId, pageable);
        } else if (nomeFiltro != null && tipo != null) {
            usuarios = usuarioRepository.findByNomeContainingIgnoreCaseAndTipo(nomeFiltro, tipo, pageable);
        } else if (nomeFiltro != null && setorId != null) {
            usuarios = usuarioRepository.findByNomeContainingIgnoreCaseAndSetor_Id(nomeFiltro, setorId, pageable);
        } else if (tipo != null && setorId != null) {
            usuarios = usuarioRepository.findByTipoAndSetor_Id(tipo, setorId, pageable);
        } else if (nomeFiltro != null) {
            usuarios = usuarioRepository.findByNomeContainingIgnoreCase(nomeFiltro, pageable);
        } else if (tipo != null) {
            usuarios = usuarioRepository.findByTipo(tipo, pageable);
        } else if (setorId != null) {
            usuarios = usuarioRepository.findBySetor_Id(setorId, pageable);
        } else {
            usuarios = usuarioRepository.findAll(pageable);
        }

        return usuarios.map(this::resposta);
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = obter(id);
        preencher(usuario, request);
        validarEmailUnico(request.email(), id);
        return resposta(usuarioRepository.save(usuario));
    }

    private Usuario obter(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + id));
    }

    private void preencher(Usuario usuario, UsuarioRequest request) {
        if (request.tipo() == null) {
            throw new IllegalArgumentException("O tipo de usuário é obrigatório.");
        }
        if (request.tipo() == TipoUsuario.TECNICO && request.setorId() == null) {
            throw new IllegalArgumentException("Técnicos devem estar vinculados a um setor.");
        }

        Setor setor = request.setorId() == null ? null : setorService.buscarPorId(request.setorId());
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setMatricula(request.matricula());
        usuario.setTipo(request.tipo());
        usuario.setSetor(setor);
    }

    private void validarEmailUnico(String email, Long idIgnorado) {
        boolean duplicado = idIgnorado == null
                ? usuarioRepository.existsByEmailIgnoreCase(email)
                : usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, idIgnorado);
        if (duplicado) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com este e-mail.");
        }
    }

    private UsuarioResponse resposta(Usuario usuario) {
        Long setorId = usuario.getSetor() == null ? null : usuario.getSetor().getId();
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getMatricula(), usuario.getTipo(), setorId);
    }
}
