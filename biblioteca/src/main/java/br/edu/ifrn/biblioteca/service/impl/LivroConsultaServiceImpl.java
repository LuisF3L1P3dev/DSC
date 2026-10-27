package br.edu.ifrn.biblioteca.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifrn.biblioteca.model.Livro;
import br.edu.ifrn.biblioteca.repository.LivroRepository;
import br.edu.ifrn.biblioteca.service.LivroConsultaService;

@Service
@Transactional(readOnly = true)
public class LivroConsultaServiceImpl implements LivroConsultaService {

    private final LivroRepository livroRepository;

    public LivroConsultaServiceImpl(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    @Override
    public List<Livro> listarDisponiveis() {
        return livroRepository.findByQuantidadeDisponivelGreaterThanOrderByTituloAsc(0);
    }

    @Override
    public List<Livro> buscarPorCategoria(String nomeCategoria) {
        return livroRepository.findByCategoriaNomeIgnoreCaseOrderByTituloAsc(nomeCategoria);
    }

    @Override
    public List<Livro> buscarPorAutor(String nomeAutor) {
        return livroRepository.findDistinctByAutoresNomeIgnoreCaseOrderByAnoPublicacaoAsc(nomeAutor);
    }
}
