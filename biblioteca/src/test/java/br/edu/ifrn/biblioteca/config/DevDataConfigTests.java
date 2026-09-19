package br.edu.ifrn.biblioteca.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import br.edu.ifrn.biblioteca.repository.CategoriaRepository;
import br.edu.ifrn.biblioteca.repository.EmprestimoRepository;
import br.edu.ifrn.biblioteca.repository.LivroRepository;
import br.edu.ifrn.biblioteca.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class DevDataConfigTests {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    @Qualifier("carregarDadosDeDemonstracao")
    private CommandLineRunner dataLoader;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCarregarCenarioCompletoUmaUnicaVez() throws Exception {
        assertThat(categoriaRepository.count()).isEqualTo(3);
        assertThat(livroRepository.count()).isEqualTo(3);
        assertThat(usuarioRepository.count()).isEqualTo(2);
        assertThat(emprestimoRepository.count()).isEqualTo(3);

        dataLoader.run();

        assertThat(categoriaRepository.count()).isEqualTo(3);
        assertThat(livroRepository.count()).isEqualTo(3);
        assertThat(usuarioRepository.count()).isEqualTo(2);
        assertThat(emprestimoRepository.count()).isEqualTo(3);
    }

    @Test
    void deveExporDadosDeDemonstracaoPelasRotas() throws Exception {
        mockMvc.perform(get("/api/livros/disponiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/emprestimos/atrasados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/categorias/quantidade-livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[2].nomeCategoria").value("Sem livros"))
                .andExpect(jsonPath("$[2].quantidadeLivros").value(0));
    }

    @Test
    void deveDisponibilizarDocumentoOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API do Sistema de Biblioteca"))
                .andExpect(jsonPath("$.paths['/api/livros/disponiveis']").exists());
    }
}
