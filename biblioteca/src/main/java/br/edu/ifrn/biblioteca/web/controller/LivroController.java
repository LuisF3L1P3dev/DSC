package br.edu.ifrn.biblioteca.web.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifrn.biblioteca.service.LivroConsultaService;
import br.edu.ifrn.biblioteca.web.dto.LivroResponse;
import br.edu.ifrn.biblioteca.web.mapper.ApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;

@Validated
@RestController
@RequestMapping("/api/livros")
@Tag(name = "Livros", description = "Consultas do acervo")
public class LivroController {

    private final LivroConsultaService livroConsultaService;

    public LivroController(LivroConsultaService livroConsultaService) {
        this.livroConsultaService = livroConsultaService;
    }

    @GetMapping("/disponiveis")
    @Operation(summary = "Lista os livros com exemplares disponíveis")
    public List<LivroResponse> listarDisponiveis() {
        return livroConsultaService.listarDisponiveis().stream()
                .map(ApiMapper::toLivroResponse)
                .toList();
    }

    @GetMapping("/por-categoria")
    @Operation(summary = "Busca livros pelo nome da categoria")
    public List<LivroResponse> buscarPorCategoria(
            @RequestParam @NotBlank(message = "O nome da categoria é obrigatório") String nome
    ) {
        return livroConsultaService.buscarPorCategoria(nome).stream()
                .map(ApiMapper::toLivroResponse)
                .toList();
    }

    @GetMapping("/por-autor")
    @Operation(summary = "Busca livros pelo nome do autor")
    public List<LivroResponse> buscarPorAutor(
            @RequestParam @NotBlank(message = "O nome do autor é obrigatório") String nome
    ) {
        return livroConsultaService.buscarPorAutor(nome).stream()
                .map(ApiMapper::toLivroResponse)
                .toList();
    }
}
