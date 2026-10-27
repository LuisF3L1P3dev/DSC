package br.edu.ifrn.biblioteca.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifrn.biblioteca.repository.CategoriaRepository;
import br.edu.ifrn.biblioteca.service.CategoriaConsultaService;
import br.edu.ifrn.biblioteca.service.CategoriaQuantidade;

@Service
@Transactional(readOnly = true)
public class CategoriaConsultaServiceImpl implements CategoriaConsultaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaConsultaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<CategoriaQuantidade> contarLivros() {
        return categoriaRepository.contarLivrosPorCategoria().stream()
                .map(resultado -> new CategoriaQuantidade(
                        resultado.getNomeCategoria(),
                        resultado.getQuantidadeLivros()
                ))
                .toList();
    }
}
