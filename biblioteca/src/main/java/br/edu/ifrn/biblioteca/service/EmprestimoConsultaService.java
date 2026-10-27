package br.edu.ifrn.biblioteca.service;

import java.util.List;

import br.edu.ifrn.biblioteca.model.Emprestimo;

public interface EmprestimoConsultaService {

    List<Emprestimo> listarAtivosDoUsuario(Long usuarioId);

    List<Emprestimo> listarAtrasados();
}
