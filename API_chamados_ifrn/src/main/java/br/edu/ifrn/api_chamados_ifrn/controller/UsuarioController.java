package br.edu.ifrn.api_chamados_ifrn.controller;

import br.edu.ifrn.api_chamados_ifrn.dto.PaginaResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.UsuarioRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.UsuarioResponse;
import br.edu.ifrn.api_chamados_ifrn.model.TipoUsuario;
import br.edu.ifrn.api_chamados_ifrn.service.UsuarioService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse resposta = usuarioService.cadastrar(request);
        return ResponseEntity.created(URI.create("/usuarios/" + resposta.id())).body(resposta);
    }

    @GetMapping
    public PaginaResponse<UsuarioResponse> listar(
            @RequestParam(name = "nome", required = false) String nome,
            @RequestParam(name = "tipo", required = false) TipoUsuario tipo,
            @RequestParam(name = "setorId", required = false) Long setorId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return PaginaResponse.de(usuarioService.listar(nome, tipo, setorId, pageable));
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable("id") Long id) {
        return usuarioService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable("id") Long id, @Valid @RequestBody UsuarioRequest request) {
        return usuarioService.atualizar(id, request);
    }
}
