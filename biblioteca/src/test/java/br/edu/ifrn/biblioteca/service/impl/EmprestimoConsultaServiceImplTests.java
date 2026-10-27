package br.edu.ifrn.biblioteca.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifrn.biblioteca.model.Emprestimo;
import br.edu.ifrn.biblioteca.model.StatusEmprestimo;
import br.edu.ifrn.biblioteca.repository.EmprestimoRepository;

@ExtendWith(MockitoExtension.class)
class EmprestimoConsultaServiceImplTests {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 18);

    @Mock
    private EmprestimoRepository emprestimoRepository;

    private EmprestimoConsultaServiceImpl service;

    @BeforeEach
    void configurarService() {
        Clock relogioFixo = Clock.fixed(
                Instant.parse("2026-09-18T12:00:00Z"),
                ZoneId.of("America/Sao_Paulo")
        );
        service = new EmprestimoConsultaServiceImpl(emprestimoRepository, relogioFixo);
    }

    @Test
    void deveUsarStatusAtivoAoBuscarPorUsuario() {
        Emprestimo emprestimo = new Emprestimo();
        when(emprestimoRepository.findDistinctByUsuarioIdAndStatusOrderByDataEmprestimoDesc(
                10L,
                StatusEmprestimo.ATIVO
        )).thenReturn(List.of(emprestimo));

        List<Emprestimo> resultado = service.listarAtivosDoUsuario(10L);

        assertThat(resultado).containsExactly(emprestimo);
        verify(emprestimoRepository).findDistinctByUsuarioIdAndStatusOrderByDataEmprestimoDesc(
                10L,
                StatusEmprestimo.ATIVO
        );
    }

    @Test
    void deveUsarDataDoRelogioInjetadoAoBuscarAtrasados() {
        service.listarAtrasados();

        verify(emprestimoRepository)
                .findDistinctByDataDevolucaoPrevistaBeforeAndStatusOrderByDataDevolucaoPrevistaAsc(
                        HOJE,
                        StatusEmprestimo.ATIVO
                );
    }
}
