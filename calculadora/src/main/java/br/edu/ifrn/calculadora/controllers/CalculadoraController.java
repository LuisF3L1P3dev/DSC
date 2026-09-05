package br.edu.ifrn.calculadora.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// recebe requisições HTTP e resultado no corpo da resposta.
@RestController
// início de todas as URLs deste controller.
@RequestMapping("/calculadora")
public class CalculadoraController {

    @GetMapping("/somar/{numero1}/{numero2}")
    public int somar(@PathVariable int numero1, @PathVariable int numero2) {
        // Os valores entre chaves na URL chegam ao método por meio de @PathVariable.
        return numero1 + numero2;
    }
}
