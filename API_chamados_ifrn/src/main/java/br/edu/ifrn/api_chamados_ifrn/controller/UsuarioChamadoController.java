package br.edu.ifrn.api_chamados_ifrn.controller;

import br.edu.ifrn.api_chamados_ifrn.dto.ChamadoResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.PaginaResponse;
import br.edu.ifrn.api_chamados_ifrn.model.Status;
import br.edu.ifrn.api_chamados_ifrn.service.ChamadoService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioChamadoController {

    private final ChamadoService chamadoService;

    public UsuarioChamadoController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @GetMapping("/{id}/chamados")
    public PaginaResponse<ChamadoResponse> listarPorUsuario(
            @PathVariable("id") Long usuarioId,
            @RequestParam(name = "status", required = false) Status status,
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return PaginaResponse.de(chamadoService.listarPorUsuario(usuarioId, status, pageable));
    }
}
