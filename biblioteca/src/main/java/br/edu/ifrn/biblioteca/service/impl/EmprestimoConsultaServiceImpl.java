package br.edu.ifrn.biblioteca.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifrn.biblioteca.model.Emprestimo;
import br.edu.ifrn.biblioteca.model.StatusEmprestimo;
import br.edu.ifrn.biblioteca.repository.EmprestimoRepository;
import br.edu.ifrn.biblioteca.service.EmprestimoConsultaService;

@Service
@Transactional(readOnly = true)
public class EmprestimoConsultaServiceImpl implements EmprestimoConsultaService {

    private final EmprestimoRepository emprestimoRepository;
    private final Clock clock;

    public EmprestimoConsultaServiceImpl(EmprestimoRepository emprestimoRepository, Clock clock) {
        this.emprestimoRepository = emprestimoRepository;
        this.clock = clock;
    }

    @Override
    public List<Emprestimo> listarAtivosDoUsuario(Long usuarioId) {
        return emprestimoRepository.findDistinctByUsuarioIdAndStatusOrderByDataEmprestimoDesc(
                usuarioId,
                StatusEmprestimo.ATIVO
        );
    }

    @Override
    public List<Emprestimo> listarAtrasados() {
        LocalDate hoje = LocalDate.now(clock);
        return emprestimoRepository
                .findDistinctByDataDevolucaoPrevistaBeforeAndStatusOrderByDataDevolucaoPrevistaAsc(
                        hoje,
                        StatusEmprestimo.ATIVO
                );
    }
}
