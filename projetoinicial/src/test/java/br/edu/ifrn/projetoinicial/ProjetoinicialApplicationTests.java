package br.edu.ifrn.projetoinicial;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjetoinicialApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void deveListarTodosOsCursosSemFiltro() throws Exception {
        mockMvc.perform(get("/api/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void deveBuscarPorTrechoDoNomeSemDiferenciarMaiusculasEMinusculas() throws Exception {
        mockMvc.perform(get("/api/cursos").param("nome", "qui"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("QUIMICA"));
    }

    @Test
    void deveRetornarListaVaziaQuandoNomeNaoForEncontrado() throws Exception {
        mockMvc.perform(get("/api/cursos").param("nome", "inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveContinuarBuscandoCursoPorId() throws Exception {
        mockMvc.perform(get("/api/cursos/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("QUIMICA"));
    }

}
