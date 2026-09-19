package br.edu.ifrn.biblioteca.web.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifrn.biblioteca.service.EmprestimoConsultaService;
import br.edu.ifrn.biblioteca.service.UsuarioConsultaService;
import br.edu.ifrn.biblioteca.web.dto.EmprestimoResponse;
import br.edu.ifrn.biblioteca.web.dto.UsuarioResponse;
import br.edu.ifrn.biblioteca.web.mapper.ApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Consultas de usuários e seus empréstimos")
public class UsuarioController {

    private final UsuarioConsultaService usuarioConsultaService;
    private final EmprestimoConsultaService emprestimoConsultaService;

    public UsuarioController(
            UsuarioConsultaService usuarioConsultaService,
            EmprestimoConsultaService emprestimoConsultaService
    ) {
        this.usuarioConsultaService = usuarioConsultaService;
        this.emprestimoConsultaService = emprestimoConsultaService;
    }

    @GetMapping("/por-nome")
    @Operation(summary = "Busca usuários por parte do nome")
    public List<UsuarioResponse> buscarPorNome(
            @RequestParam @NotBlank(message = "O trecho do nome é obrigatório") String trecho
    ) {
        return usuarioConsultaService.buscarPorNome(trecho).stream()
                .map(ApiMapper::toUsuarioResponse)
                .toList();
    }

    @GetMapping("/{id}/emprestimos/ativos")
    @Operation(summary = "Lista os empréstimos ativos de um usuário")
    public List<EmprestimoResponse> listarEmprestimosAtivos(
            @PathVariable @Positive(message = "O ID deve ser positivo") Long id
    ) {
        return emprestimoConsultaService.listarAtivosDoUsuario(id).stream()
                .map(ApiMapper::toEmprestimoResponse)
                .toList();
    }
}
