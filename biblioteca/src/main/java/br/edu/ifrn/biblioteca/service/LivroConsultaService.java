package br.edu.ifrn.biblioteca.service;

import java.util.List;

import br.edu.ifrn.biblioteca.model.Livro;

public interface LivroConsultaService {

    List<Livro> listarDisponiveis();

    List<Livro> buscarPorCategoria(String nomeCategoria);

    List<Livro> buscarPorAutor(String nomeAutor);
}
