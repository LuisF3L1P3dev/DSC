package br.edu.ifrn.api_chamados_ifrn.repository;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.ifrn.api_chamados_ifrn.model.Chamado;
import br.edu.ifrn.api_chamados_ifrn.model.Prioridade;
import br.edu.ifrn.api_chamados_ifrn.model.Status;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ChamadoRepositoryTest {

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final PageRequest pagina = PageRequest.of(0, 10, Sort.by("id").ascending());

    @BeforeEach
    void inserirChamadosDeTeste() {
        inserir("Rede urgente", Prioridade.ALTA, Status.ABERTO,
                LocalDateTime.of(2026, 1, 5, 10, 0), 1L, 1L, 3L);
        inserir("Manutenção agendada", Prioridade.MEDIA, Status.EM_ATENDIMENTO,
                LocalDateTime.of(2026, 1, 10, 10, 0), 1L, 3L, 4L);
        inserir("Computador cancelado", Prioridade.BAIXA, Status.CANCELADO,
                LocalDateTime.of(2026, 2, 1, 10, 0), 2L, 2L, null);
        inserir("Rede solicitante", Prioridade.MEDIA, Status.ABERTO,
                LocalDateTime.of(2026, 3, 1, 10, 0), 2L, 1L, 3L);
    }

    @Test
    void aplicaFiltrosOpcionaisIsoladosECombinados() {
        assertThat(buscar(null, null, null, null, null, null, null, null).getTotalElements()).isEqualTo(4);
        assertThat(buscar(Status.ABERTO, null, null, null, null, null, null, null).getTotalElements()).isEqualTo(2);
        assertThat(buscar(null, Prioridade.ALTA, null, null, null, null, null, null).getTotalElements()).isEqualTo(1);
        assertThat(buscar(null, null, 1L, null, null, null, null, null).getTotalElements()).isEqualTo(3);
        assertThat(buscar(null, null, null, 3L, null, null, null, null).getTotalElements()).isEqualTo(1);
        assertThat(buscar(null, null, null, null, 1L, null, null, null).getTotalElements()).isEqualTo(2);
        assertThat(buscar(null, null, null, null, null, 3L, null, null).getTotalElements()).isEqualTo(2);

        LocalDateTime inicio = LocalDateTime.of(2026, 1, 10, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 2, 1, 10, 0);
        assertThat(buscar(null, null, null, null, null, null, inicio, null).getTotalElements()).isEqualTo(3);
        assertThat(buscar(null, null, null, null, null, null, null, inicio).getTotalElements()).isEqualTo(2);
        assertThat(buscar(null, null, null, null, null, null, inicio, fim).getTotalElements()).isEqualTo(2);
        assertThat(buscar(Status.ABERTO, Prioridade.ALTA, 1L, 1L, 1L, 3L,
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 31, 23, 59))
                .getTotalElements()).isEqualTo(1);
    }

    @Test
    void listaChamadosPorSolicitanteOuTecnicoComFiltroDeStatus() {
        assertThat(chamadoRepository.findBySolicitante_Id(1L, pagina).getTotalElements()).isEqualTo(2);
        assertThat(chamadoRepository.findBySolicitante_IdAndStatus(1L, Status.ABERTO, pagina)
                .getTotalElements()).isEqualTo(1);
        assertThat(chamadoRepository.findByTecnico_Id(3L, pagina).getTotalElements()).isEqualTo(2);
        assertThat(chamadoRepository.findByTecnico_IdAndStatus(3L, Status.ABERTO, pagina)
                .getTotalElements()).isEqualTo(2);
    }

    @Test
    void consultasPaginadasRetornamItensEMetadados() {
        Page<Chamado> resultado = chamadoRepository.findBySolicitante_Id(
                1L, PageRequest.of(0, 1, Sort.by("id").ascending()));

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getTotalElements()).isEqualTo(2);
        assertThat(resultado.getTotalPages()).isEqualTo(2);
    }

    private Page<Chamado> buscar(Status status, Prioridade prioridade, Long setorId, Long categoriaId,
            Long solicitanteId, Long tecnicoId, LocalDateTime dataInicio, LocalDateTime dataFim) {
        return chamadoRepository.buscarComFiltros(status, prioridade, setorId, categoriaId,
                solicitanteId, tecnicoId, dataInicio, dataFim, pagina);
    }

    private void inserir(String titulo, Prioridade prioridade, Status status, LocalDateTime dataAbertura,
            Long solicitanteId, Long categoriaId, Long tecnicoId) {
        jdbcTemplate.update("""
                INSERT INTO chamado (titulo, descricao, local, prioridade, status, data_abertura,
                                     solicitante_id, categoria_id, tecnico_id)
                VALUES (?, 'Descrição de teste', 'Sala 1', ?, ?, ?, ?, ?, ?)
                """, titulo, prioridade.name(), status.name(), Timestamp.valueOf(dataAbertura),
                solicitanteId, categoriaId, tecnicoId);
    }
}
