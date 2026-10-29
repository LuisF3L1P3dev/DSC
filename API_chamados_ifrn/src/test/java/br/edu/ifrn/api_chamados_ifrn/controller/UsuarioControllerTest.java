package br.edu.ifrn.api_chamados_ifrn.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.jpa.open-in-view=false")
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void cadastraConsultaEAtualizaUsuario() throws Exception {
        MvcResult criada = mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Joao Silva","email":"joao@example.com",
                                 "matricula":"2026100","tipo":"SOLICITANTE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/usuarios/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nome").value("Joao Silva"))
                .andExpect(jsonPath("$.setorId").value(nullValue()))
                .andReturn();

        String caminho = criada.getResponse().getHeader("Location");
        mvc.perform(get(caminho))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao@example.com"));
        mvc.perform(put(caminho).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Joao Atualizado","email":"joao@example.com",
                                 "matricula":"2026100","tipo":"SOLICITANTE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joao Atualizado"))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void listaComFiltrosCombinadosEPaginacao() throws Exception {
        mvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tamanhoPagina").value(10))
                .andExpect(jsonPath("$.totalElementos").value(4));
        mvc.perform(get("/usuarios").param("nome", "aN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(get("/usuarios").param("tipo", "TECNICO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(get("/usuarios").param("setorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].nome").value("Carlos"))
                .andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(get("/usuarios").param("nome", "c").param("tipo", "TECNICO").param("setorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].nome").value("Carlos"))
                .andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(get("/usuarios").param("size", "100").param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].nome").value("Daniela"))
                .andExpect(jsonPath("$.tamanhoPagina").value(50));
    }

    @Test
    void devolveErrosPadronizadosParaDadosInvalidosEConflitos() throws Exception {
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","email":"email-invalido","matricula":"1",
                                 "tipo":"SOLICITANTE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.caminho").value("/usuarios"));
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Carlos","email":"carlos-novo@example.com","matricula":"2",
                                 "tipo":"TECNICO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Novo","email":"ANA@EXAMPLE.COM","matricula":"3",
                                 "tipo":"SOLICITANTE"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Carlos","email":"carlos-novo@example.com","matricula":"2",
                                 "tipo":"TECNICO","setorId":999999}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mvc.perform(get("/usuarios/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
