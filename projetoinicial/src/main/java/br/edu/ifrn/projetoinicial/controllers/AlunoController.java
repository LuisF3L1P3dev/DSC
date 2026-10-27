package br.edu.ifrn.projetoinicial.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifrn.projetoinicial.models.Aluno;

@RestController
@RequestMapping("api/alunos")
public class AlunoController {

    private List<Aluno> alunos = new ArrayList<>();

    public AlunoController() {
        alunos.add(new Aluno(1, "Jeferson", "jeferson@ifrn.edu.br"));
        alunos.add(new Aluno(2, "Maria", "maria@ifrn.edu.br"));
        alunos.add(new Aluno(3, "Joao", "joao@ifrn.edu.br"));
    }

    // GET http://localhost:8080/api/alunos
    @GetMapping
    public List<Aluno> listar() {
        return alunos;
    }

    // GET http://localhost:8080/api/alunos/1
    @GetMapping("/{id}")
    public Aluno buscarPorId(@PathVariable int id) {
        for (Aluno aluno : alunos) {
            if (aluno.id() == id) {
                return aluno;
            }
        }
        return null;
    }

}