package br.edu.ifrn.biblioteca.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifrn.biblioteca.model.Usuario;
import br.edu.ifrn.biblioteca.repository.UsuarioRepository;
import br.edu.ifrn.biblioteca.service.UsuarioConsultaService;

@Service
@Transactional(readOnly = true)
public class UsuarioConsultaServiceImpl implements UsuarioConsultaService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioConsultaServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Usuario> buscarPorNome(String trechoNome) {
        return usuarioRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(trechoNome);
    }
}
