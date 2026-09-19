package br.edu.ifrn.biblioteca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.edu.ifrn.biblioteca.model.Categoria;
import br.edu.ifrn.biblioteca.repository.projection.CategoriaQuantidadeProjection;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    @Query("""
            select c.nome as nomeCategoria, count(l) as quantidadeLivros
            from Categoria c
            left join c.livros l
            group by c.id, c.nome
            order by count(l) desc, c.nome asc
            """)
    List<CategoriaQuantidadeProjection> contarLivrosPorCategoria();
}
