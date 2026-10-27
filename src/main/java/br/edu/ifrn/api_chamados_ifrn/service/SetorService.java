package br.edu.ifrn.api_chamados_ifrn.service;

import br.edu.ifrn.api_chamados_ifrn.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.api_chamados_ifrn.exception.RegraNegocioException;
import br.edu.ifrn.api_chamados_ifrn.model.Setor;
import br.edu.ifrn.api_chamados_ifrn.repository.CategoriaRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.ChamadoRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.SetorRepository;
import br.edu.ifrn.api_chamados_ifrn.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetorService {

    private final SetorRepository setorRepository;
    private final CategoriaRepository categoriaRepository;
    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;

    public SetorService(SetorRepository setorRepository, CategoriaRepository categoriaRepository,
            ChamadoRepository chamadoRepository, UsuarioRepository usuarioRepository) {
        this.setorRepository = setorRepository;
        this.categoriaRepository = categoriaRepository;
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Setor cadastrar(String nome, String sigla) {
        validarDados(nome, sigla);
        Setor setor = new Setor();
        setor.setNome(nome);
        setor.setSigla(sigla);
        return setorRepository.save(setor);
    }

    @Transactional(readOnly = true)
    public Setor buscarPorId(Long id) {
        return setorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Setor> listar(String nome, Pageable pageable) {
        if (nome == null || nome.isBlank()) {
            return setorRepository.findAll(pageable);
        }
        return setorRepository.findByNomeContainingIgnoreCase(nome, pageable);
    }

    @Transactional
    public Setor atualizar(Long id, String nome, String sigla) {
        validarDados(nome, sigla);
        Setor setor = buscarPorId(id);
        setor.setNome(nome);
        setor.setSigla(sigla);
        return setorRepository.save(setor);
    }

    @Transactional
    public void excluir(Long id) {
        Setor setor = buscarPorId(id);
        if (chamadoRepository.existsByCategoria_Setor_Id(id)) {
            throw new RegraNegocioException("Não é possível excluir um setor com chamados vinculados.");
        }
        if (categoriaRepository.existsBySetor_Id(id)) {
            throw new RegraNegocioException("Não é possível excluir um setor com categorias vinculadas.");
        }
        if (usuarioRepository.existsBySetor_Id(id)) {
            throw new RegraNegocioException("Não é possível excluir um setor com usuários vinculados.");
        }
        setorRepository.delete(setor);
    }

    private void validarDados(String nome, String sigla) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do setor é obrigatório.");
        }
        if (sigla == null || sigla.isBlank()) {
            throw new IllegalArgumentException("A sigla do setor é obrigatória.");
        }
    }
}
