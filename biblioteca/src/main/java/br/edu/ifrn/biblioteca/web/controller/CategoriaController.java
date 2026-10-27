package br.edu.ifrn.biblioteca.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifrn.biblioteca.service.CategoriaConsultaService;
import br.edu.ifrn.biblioteca.web.dto.CategoriaQuantidadeResponse;
import br.edu.ifrn.biblioteca.web.mapper.ApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Consultas agregadas de categorias")
public class CategoriaController {

    private final CategoriaConsultaService categoriaConsultaService;

    public CategoriaController(CategoriaConsultaService categoriaConsultaService) {
        this.categoriaConsultaService = categoriaConsultaService;
    }

    @GetMapping("/quantidade-livros")
    @Operation(summary = "Conta a quantidade de livros por categoria")
    public List<CategoriaQuantidadeResponse> contarLivros() {
        return categoriaConsultaService.contarLivros().stream()
                .map(ApiMapper::toCategoriaQuantidadeResponse)
                .toList();
    }
}
