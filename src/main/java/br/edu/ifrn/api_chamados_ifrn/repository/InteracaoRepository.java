package br.edu.ifrn.api_chamados_ifrn.repository;

import br.edu.ifrn.api_chamados_ifrn.model.Interacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteracaoRepository extends JpaRepository<Interacao, Long> {
}
