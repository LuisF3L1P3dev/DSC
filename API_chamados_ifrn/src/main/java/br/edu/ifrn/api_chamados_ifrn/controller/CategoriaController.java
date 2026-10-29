package br.edu.ifrn.api_chamados_ifrn.controller;

import br.edu.ifrn.api_chamados_ifrn.dto.CategoriaRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.CategoriaResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.PaginaResponse;
import br.edu.ifrn.api_chamados_ifrn.service.CategoriaService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> cadastrar(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse resposta = categoriaService.cadastrar(request);
        return ResponseEntity.created(URI.create("/categorias/" + resposta.id())).body(resposta);
    }

    @GetMapping
    public PaginaResponse<CategoriaResponse> listar(@RequestParam(name = "nome", required = false) String nome,
            @RequestParam(name = "setorId", required = false) Long setorId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return PaginaResponse.de(categoriaService.listar(nome, setorId, pageable));
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscar(@PathVariable("id") Long id) {
        return categoriaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public CategoriaResponse atualizar(@PathVariable("id") Long id, @Valid @RequestBody CategoriaRequest request) {
        return categoriaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
