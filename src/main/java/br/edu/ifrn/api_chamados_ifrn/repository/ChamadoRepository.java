package br.edu.ifrn.api_chamados_ifrn.repository;

import br.edu.ifrn.api_chamados_ifrn.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
}
