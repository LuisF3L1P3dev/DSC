package br.edu.ifrn.biblioteca.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifrn.biblioteca.service.EmprestimoConsultaService;
import br.edu.ifrn.biblioteca.web.dto.EmprestimoResponse;
import br.edu.ifrn.biblioteca.web.mapper.ApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/emprestimos")
@Tag(name = "Empréstimos", description = "Consultas gerais de empréstimos")
public class EmprestimoController {

    private final EmprestimoConsultaService emprestimoConsultaService;

    public EmprestimoController(EmprestimoConsultaService emprestimoConsultaService) {
        this.emprestimoConsultaService = emprestimoConsultaService;
    }

    @GetMapping("/atrasados")
    @Operation(summary = "Lista os empréstimos ativos com prazo vencido")
    public List<EmprestimoResponse> listarAtrasados() {
        return emprestimoConsultaService.listarAtrasados().stream()
                .map(ApiMapper::toEmprestimoResponse)
                .toList();
    }
}
