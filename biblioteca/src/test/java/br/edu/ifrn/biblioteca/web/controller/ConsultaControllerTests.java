package br.edu.ifrn.biblioteca.web.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.edu.ifrn.biblioteca.model.Autor;
import br.edu.ifrn.biblioteca.model.Categoria;
import br.edu.ifrn.biblioteca.model.Emprestimo;
import br.edu.ifrn.biblioteca.model.Livro;
import br.edu.ifrn.biblioteca.model.StatusEmprestimo;
import br.edu.ifrn.biblioteca.model.Usuario;
import br.edu.ifrn.biblioteca.service.CategoriaConsultaService;
import br.edu.ifrn.biblioteca.service.CategoriaQuantidade;
import br.edu.ifrn.biblioteca.service.EmprestimoConsultaService;
import br.edu.ifrn.biblioteca.service.LivroConsultaService;
import br.edu.ifrn.biblioteca.service.UsuarioConsultaService;

@WebMvcTest({
        LivroController.class,
        UsuarioController.class,
        EmprestimoController.class,
        CategoriaController.class
})
class ConsultaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LivroConsultaService livroConsultaService;

    @MockitoBean
    private UsuarioConsultaService usuarioConsultaService;

    @MockitoBean
    private EmprestimoConsultaService emprestimoConsultaService;

    @MockitoBean
    private CategoriaConsultaService categoriaConsultaService;

    private Livro livro;
    private Usuario usuario;
    private Emprestimo emprestimo;

    @BeforeEach
    void prepararObjetos() {
        Categoria categoria = new Categoria();
        categoria.setNome("Tecnologia");

        Autor autor = new Autor();
        autor.setNome("Ana Código");

        livro = new Livro();
        livro.setIsbn("9780000000001");
        livro.setTitulo("Arquitetura Limpa");
        livro.setEditora("Editora Técnica");
        livro.setAnoPublicacao(2017);
        livro.setNumeroPaginas(350);
        livro.setQuantidadeTotal(3);
        livro.setQuantidadeDisponivel(2);
        livro.setCategoria(categoria);
        livro.adicionarAutor(autor);

        usuario = new Usuario();
        usuario.setNome("João Silva");
        usuario.setCpf("111.111.111-11");
        usuario.setEmail("joao@biblioteca.dev");
        usuario.setDataCadastro(LocalDate.of(2026, 1, 1));
        usuario.setAtivo(true);

        emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(LocalDate.of(2026, 9, 1));
        emprestimo.setDataDevolucaoPrevista(LocalDate.of(2026, 9, 10));
        emprestimo.setStatus(StatusEmprestimo.ATIVO);
        emprestimo.setValorMulta(BigDecimal.ZERO);
        emprestimo.adicionarLivro(livro);
    }

    @Test
    void deveListarLivrosDisponiveis() throws Exception {
        when(livroConsultaService.listarDisponiveis()).thenReturn(List.of(livro));

        mockMvc.perform(get("/api/livros/disponiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Arquitetura Limpa"))
                .andExpect(jsonPath("$[0].categoria.nome").value("Tecnologia"))
                .andExpect(jsonPath("$[0].autores[0].nome").value("Ana Código"));

        verify(livroConsultaService).listarDisponiveis();
    }

    @Test
    void deveBuscarLivrosPorCategoria() throws Exception {
        when(livroConsultaService.buscarPorCategoria("Tecnologia")).thenReturn(List.of(livro));

        mockMvc.perform(get("/api/livros/por-categoria").param("nome", "Tecnologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Arquitetura Limpa"));

        verify(livroConsultaService).buscarPorCategoria("Tecnologia");
    }

    @Test
    void deveBuscarLivrosPorAutor() throws Exception {
        when(livroConsultaService.buscarPorAutor("Ana Código")).thenReturn(List.of(livro));

        mockMvc.perform(get("/api/livros/por-autor").param("nome", "Ana Código"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Arquitetura Limpa"));

        verify(livroConsultaService).buscarPorAutor("Ana Código");
    }

    @Test
    void deveBuscarUsuariosPorParteDoNome() throws Exception {
        when(usuarioConsultaService.buscarPorNome("Silva")).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios/por-nome").param("trecho", "Silva"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("João Silva"))
                .andExpect(jsonPath("$[0].email").value("joao@biblioteca.dev"));

        verify(usuarioConsultaService).buscarPorNome("Silva");
    }

    @Test
    void deveListarEmprestimosAtivosDoUsuario() throws Exception {
        when(emprestimoConsultaService.listarAtivosDoUsuario(1L)).thenReturn(List.of(emprestimo));

        mockMvc.perform(get("/api/usuarios/1/emprestimos/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuario.nome").value("João Silva"))
                .andExpect(jsonPath("$[0].livros[0].titulo").value("Arquitetura Limpa"));

        verify(emprestimoConsultaService).listarAtivosDoUsuario(1L);
    }

    @Test
    void deveListarEmprestimosAtrasados() throws Exception {
        when(emprestimoConsultaService.listarAtrasados()).thenReturn(List.of(emprestimo));

        mockMvc.perform(get("/api/emprestimos/atrasados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ATIVO"))
                .andExpect(jsonPath("$[0].dataDevolucaoPrevista").value("2026-09-10"));

        verify(emprestimoConsultaService).listarAtrasados();
    }

    @Test
    void deveContarLivrosPorCategoria() throws Exception {
        when(categoriaConsultaService.contarLivros())
                .thenReturn(List.of(new CategoriaQuantidade("Tecnologia", 2)));

        mockMvc.perform(get("/api/categorias/quantidade-livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCategoria").value("Tecnologia"))
                .andExpect(jsonPath("$[0].quantidadeLivros").value(2));

        verify(categoriaConsultaService).contarLivros();
    }

    @Test
    void deveRejeitarParametroTextualEmBranco() throws Exception {
        mockMvc.perform(get("/api/livros/por-categoria").param("nome", " "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarIdNaoPositivo() throws Exception {
        mockMvc.perform(get("/api/usuarios/0/emprestimos/ativos"))
                .andExpect(status().isBadRequest());
    }
}
