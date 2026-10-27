package br.edu.ifrn.biblioteca.service;

import java.util.List;

import br.edu.ifrn.biblioteca.model.Usuario;

public interface UsuarioConsultaService {

    List<Usuario> buscarPorNome(String trechoNome);
}
