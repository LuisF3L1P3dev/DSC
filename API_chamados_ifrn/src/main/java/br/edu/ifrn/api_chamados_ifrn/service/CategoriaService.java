package br.edu.ifrn.api_chamados_ifrn.service;

import br.edu.ifrn.api_chamados_ifrn.dto.CategoriaRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.CategoriaResponse;
import br.edu.ifrn.api_chamados_ifrn.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.api_chamados_ifrn.exception.RegraNegocioException;
import br.edu.ifrn.api_chamados_ifrn.model.Categoria;
import br.edu.ifrn.api_chamados_ifrn.repository.CategoriaRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.ChamadoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ChamadoRepository chamadoRepository;
    private final SetorService setorService;

    public CategoriaService(CategoriaRepository categoriaRepository, ChamadoRepository chamadoRepository,
            SetorService setorService) {
        this.categoriaRepository = categoriaRepository;
        this.chamadoRepository = chamadoRepository;
        this.setorService = setorService;
    }

    @Transactional
    public CategoriaResponse cadastrar(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        preencher(categoria, request);
        return resposta(categoriaRepository.save(categoria));
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {
        return resposta(obter(id));
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listar(String nome, Long setorId, Pageable pageable) {
        boolean filtrarNome = nome != null && !nome.isBlank();
        Page<Categoria> categorias;
        if (filtrarNome && setorId != null) {
            categorias = categoriaRepository.findByNomeContainingIgnoreCaseAndSetor_Id(nome.trim(), setorId, pageable);
        } else if (filtrarNome) {
            categorias = categoriaRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        } else if (setorId != null) {
            categorias = categoriaRepository.findBySetor_Id(setorId, pageable);
        } else {
            categorias = categoriaRepository.findAll(pageable);
        }
        return categorias.map(this::resposta);
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listarPorSetor(Long setorId, String nome, Pageable pageable) {
        setorService.buscarPorId(setorId);
        return listar(nome, setorId, pageable);
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = obter(id);
        preencher(categoria, request);
        return resposta(categoriaRepository.save(categoria));
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = obter(id);
        if (chamadoRepository.existsByCategoria_Id(id)) {
            throw new RegraNegocioException("Não é possível excluir uma categoria com chamados vinculados.");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria obter(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: " + id));
    }

    private void preencher(Categoria categoria, CategoriaRequest request) {
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        categoria.setSetor(setorService.buscarPorId(request.setorId()));
    }

    private CategoriaResponse resposta(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getDescricao(),
                categoria.getSetor().getId());
    }
}
