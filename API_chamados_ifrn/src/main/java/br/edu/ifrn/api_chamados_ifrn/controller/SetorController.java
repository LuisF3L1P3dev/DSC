package br.edu.ifrn.api_chamados_ifrn.controller;

import br.edu.ifrn.api_chamados_ifrn.dto.PaginaResponse;
import br.edu.ifrn.api_chamados_ifrn.dto.SetorRequest;
import br.edu.ifrn.api_chamados_ifrn.dto.SetorResponse;
import br.edu.ifrn.api_chamados_ifrn.model.Setor;
import br.edu.ifrn.api_chamados_ifrn.service.SetorService;
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
@RequestMapping("/setores")
public class SetorController {

    private final SetorService setorService;

    public SetorController(SetorService setorService) {
        this.setorService = setorService;
    }

    @PostMapping
    public ResponseEntity<SetorResponse> cadastrar(@Valid @RequestBody SetorRequest request) {
        Setor setor = setorService.cadastrar(request.nome(), request.sigla());
        return ResponseEntity.created(URI.create("/setores/" + setor.getId())).body(resposta(setor));
    }

    @GetMapping
    public PaginaResponse<SetorResponse> listar(@RequestParam(name = "nome", required = false) String nome,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return PaginaResponse.de(setorService.listar(nome, pageable).map(this::resposta));
    }

    @GetMapping("/{id}")
    public SetorResponse buscar(@PathVariable("id") Long id) {
        return resposta(setorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public SetorResponse atualizar(@PathVariable("id") Long id, @Valid @RequestBody SetorRequest request) {
        return resposta(setorService.atualizar(id, request.nome(), request.sigla()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        setorService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private SetorResponse resposta(Setor setor) {
        return new SetorResponse(setor.getId(), setor.getNome(), setor.getSigla());
    }
}
