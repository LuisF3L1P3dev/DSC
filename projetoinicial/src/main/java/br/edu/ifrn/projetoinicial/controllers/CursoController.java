package br.edu.ifrn.projetoinicial.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.web.bind.annotation.*;

import br.edu.ifrn.projetoinicial.models.Curso;

@RestController
@RequestMapping("api/cursos")
public class CursoController {
    private List<Curso> cursos = new ArrayList<>();

    public CursoController(){
        cursos.add(new Curso(1, "ADS", 30));
        cursos.add(new Curso(2, "QUIMICA", 40));
        cursos.add(new Curso(1, "AGRO", 50));
    }

    @GetMapping
    public List<Curso> listar(){
        return cursos;
    }

    @GetMapping("/{id}")
    public Curso buscarPorId(@PathVariable int id){
        for (Curso curso: cursos){
            if (curso.id() == id){
                return curso;
            }
        }
        return null;
    }

    @GetMapping(params = "nome")
    public List<Curso> buscarPorNome(@RequestParam String nome) {
        List<Curso> resultado = new ArrayList<>();
        String textoBusca = nome.toLowerCase(Locale.ROOT);

        for (Curso curso : cursos) {
            if (curso.nome().toLowerCase(Locale.ROOT).contains(textoBusca)) {
                resultado.add(curso);
            }
        }

        return resultado;
    }
}
