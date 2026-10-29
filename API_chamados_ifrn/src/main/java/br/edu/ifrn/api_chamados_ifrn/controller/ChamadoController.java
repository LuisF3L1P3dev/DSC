package br.edu.ifrn.api_chamados_ifrn.controller;

import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoDetalheResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoFiltro;
import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.PaginaResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.AssumirChamadoRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.ResolverChamadoRequest;
import br.edu.ifrn.api_chamados_ifrn.service.ChamadoService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;

    public ChamadoController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @PostMapping
    public ResponseEntity<ChamadoResponse> abrir(@Valid @RequestBody ChamadoRequest request) {
        ChamadoResponse resposta = chamadoService.abrir(request);
        return ResponseEntity.created(URI.create("/chamados/" + resposta.id())).body(resposta);
    }

    @PatchMapping("/{id}/assumir")
    public ChamadoResponse assumir(@PathVariable("id") Long id,
            @Valid @RequestBody AssumirChamadoRequest request) {
        return chamadoService.assumir(id, request);
    }

    @PatchMapping("/{id}/resolver")
    public ChamadoResponse resolver(@PathVariable("id") Long id,
            @Valid @RequestBody ResolverChamadoRequest request) {
        return chamadoService.resolver(id, request);
    }

    @GetMapping
    public PaginaResponse<ChamadoResponse> listar(
            @Valid @ModelAttribute ChamadoFiltro filtro,
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return PaginaResponse.de(chamadoService.listar(filtro, pageable));
    }

    @GetMapping("/{id}")
    public ChamadoDetalheResponse buscar(@PathVariable("id") Long id) {
        return chamadoService.buscarPorId(id);
    }
}
