package br.edu.ifrn.api_chamados_ifrn.controller;

import br.edu.ifrn.api_chamados_ifrn.dto.CategoriaResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.PaginaResponse;
import br.edu.ifrn.api_chamados_ifrn.service.CategoriaService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/setores/{setorId}/categorias")
public class SetorCategoriaController {

    private final CategoriaService categoriaService;

    public SetorCategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public PaginaResponse<CategoriaResponse> listar(@PathVariable("setorId") Long setorId,
            @RequestParam(name = "nome", required = false) String nome,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return PaginaResponse.de(categoriaService.listarPorSetor(setorId, nome, pageable));
    }
}
