package br.edu.ifrn.api_chamados_ifrn.repository;

import br.edu.ifrn.api_chamados_ifrn.model.Chamado;
import br.edu.ifrn.api_chamados_ifrn.model.Prioridade;
import br.edu.ifrn.api_chamados_ifrn.model.Status;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    boolean existsByCategoria_Setor_Id(Long setorId);

    boolean existsByCategoria_Id(Long categoriaId);

    @Query("""
            SELECT chamado FROM Chamado chamado
            WHERE (:status IS NULL OR chamado.status = :status)
              AND (:prioridade IS NULL OR chamado.prioridade = :prioridade)
              AND (:setorId IS NULL OR chamado.categoria.setor.id = :setorId)
              AND (:categoriaId IS NULL OR chamado.categoria.id = :categoriaId)
              AND (:solicitanteId IS NULL OR chamado.solicitante.id = :solicitanteId)
              AND (:tecnicoId IS NULL OR chamado.tecnico.id = :tecnicoId)
              AND chamado.dataAbertura >= COALESCE(:dataInicio, chamado.dataAbertura)
              AND chamado.dataAbertura <= COALESCE(:dataFim, chamado.dataAbertura)
            """)
    Page<Chamado> buscarComFiltros(
            @Param("status") Status status,
            @Param("prioridade") Prioridade prioridade,
            @Param("setorId") Long setorId,
            @Param("categoriaId") Long categoriaId,
            @Param("solicitanteId") Long solicitanteId,
            @Param("tecnicoId") Long tecnicoId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable);

    Page<Chamado> findBySolicitante_Id(Long solicitanteId, Pageable pageable);

    Page<Chamado> findBySolicitante_IdAndStatus(Long solicitanteId, Status status, Pageable pageable);

    Page<Chamado> findByTecnico_Id(Long tecnicoId, Pageable pageable);

    Page<Chamado> findByTecnico_IdAndStatus(Long tecnicoId, Status status, Pageable pageable);
}
