package br.edu.ifrn.api_chamados_ifrn.service;

import br.edu.ifrn.api_chamados_ifrn.model.Setor;
import br.edu.ifrn.api_chamados_ifrn.repository.SetorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetorService {

    private final SetorRepository setorRepository;

    public SetorService(SetorRepository setorRepository) {
        this.setorRepository = setorRepository;
    }

    @Transactional
    public Setor cadastrar(String nome, String sigla) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do setor é obrigatório.");
        }
        if (sigla == null || sigla.isBlank()) {
            throw new IllegalArgumentException("A sigla do setor é obrigatória.");
        }

        Setor setor = new Setor();
        setor.setNome(nome);
        setor.setSigla(sigla);
        return setorRepository.save(setor);
    }
}
