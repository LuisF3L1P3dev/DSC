package br.edu.ifrn.biblioteca.web.mapper;

import br.edu.ifrn.biblioteca.model.Autor;
import br.edu.ifrn.biblioteca.model.Categoria;
import br.edu.ifrn.biblioteca.model.Emprestimo;
import br.edu.ifrn.biblioteca.model.Livro;
import br.edu.ifrn.biblioteca.model.Usuario;
import br.edu.ifrn.biblioteca.service.CategoriaQuantidade;
import br.edu.ifrn.biblioteca.web.dto.AutorResumoResponse;
import br.edu.ifrn.biblioteca.web.dto.CategoriaQuantidadeResponse;
import br.edu.ifrn.biblioteca.web.dto.CategoriaResumoResponse;
import br.edu.ifrn.biblioteca.web.dto.EmprestimoResponse;
import br.edu.ifrn.biblioteca.web.dto.LivroResponse;
import br.edu.ifrn.biblioteca.web.dto.LivroResumoResponse;
import br.edu.ifrn.biblioteca.web.dto.UsuarioResponse;
import br.edu.ifrn.biblioteca.web.dto.UsuarioResumoResponse;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static LivroResponse toLivroResponse(Livro livro) {
        return new LivroResponse(
                livro.getId(),
                livro.getIsbn(),
                livro.getTitulo(),
                livro.getEditora(),
                livro.getAnoPublicacao(),
                livro.getNumeroPaginas(),
                livro.getQuantidadeTotal(),
                livro.getQuantidadeDisponivel(),
                toCategoriaResumo(livro.getCategoria()),
                livro.getAutores().stream().map(ApiMapper::toAutorResumo).toList()
        );
    }

    public static UsuarioResponse toUsuarioResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getDataCadastro(),
                usuario.getEndereco(),
                usuario.getAtivo()
        );
    }

    public static EmprestimoResponse toEmprestimoResponse(Emprestimo emprestimo) {
        return new EmprestimoResponse(
                emprestimo.getId(),
                toUsuarioResumo(emprestimo.getUsuario()),
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataDevolucaoPrevista(),
                emprestimo.getDataDevolucaoEfetiva(),
                emprestimo.getStatus(),
                emprestimo.getValorMulta(),
                emprestimo.getItens().stream()
                        .map(item -> toLivroResumo(item.getLivro()))
                        .toList()
        );
    }

    public static CategoriaQuantidadeResponse toCategoriaQuantidadeResponse(CategoriaQuantidade quantidade) {
        return new CategoriaQuantidadeResponse(
                quantidade.nomeCategoria(),
                quantidade.quantidadeLivros()
        );
    }

    private static AutorResumoResponse toAutorResumo(Autor autor) {
        return new AutorResumoResponse(autor.getId(), autor.getNome());
    }

    private static CategoriaResumoResponse toCategoriaResumo(Categoria categoria) {
        return new CategoriaResumoResponse(categoria.getId(), categoria.getNome());
    }

    private static UsuarioResumoResponse toUsuarioResumo(Usuario usuario) {
        return new UsuarioResumoResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getAtivo()
        );
    }

    private static LivroResumoResponse toLivroResumo(Livro livro) {
        return new LivroResumoResponse(
                livro.getId(),
                livro.getIsbn(),
                livro.getTitulo(),
                livro.getAnoPublicacao()
        );
    }
}
