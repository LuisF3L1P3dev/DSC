package br.edu.ifrn.api_chamados_ifrn.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SetorControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void cadastraConsultaAtualizaEExcluiSetorSemVinculos() throws Exception {
        MvcResult criado = mvc.perform(post("/setores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Biblioteca\",\"sigla\":\"BIB\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Biblioteca"))
                .andReturn();

        String caminho = criado.getResponse().getHeader("Location");
        mvc.perform(get(caminho))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sigla").value("BIB"));
        mvc.perform(put(caminho).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Biblioteca Central\",\"sigla\":\"BIB\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Biblioteca Central"));
        mvc.perform(delete(caminho)).andExpect(status().isNoContent());
        mvc.perform(get(caminho)).andExpect(status().isNotFound());
    }

    @Test
    void listaComFiltroSemDiferenciarMaiusculasEPaginaLimitada() throws Exception {
        mvc.perform(get("/setores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroPagina").value(0))
                .andExpect(jsonPath("$.tamanhoPagina").value(10))
                .andExpect(jsonPath("$.totalElementos").value(2));

        mvc.perform(get("/setores").param("nome", "TECNOLOGIA").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].sigla").value("COTIC"))
                .andExpect(jsonPath("$.tamanhoPagina").value(50))
                .andExpect(jsonPath("$.totalElementos").value(1));

        mvc.perform(get("/setores").param("size", "1").param("sort", "sigla,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].sigla").value("MANUT"))
                .andExpect(jsonPath("$.totalPaginas").value(2));
    }

    @Test
    void rejeitaEntradaInvalidaEConsultaInexistente() throws Exception {
        mvc.perform(post("/setores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"sigla\":\"BIB\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.caminho").value("/setores"));
        mvc.perform(get("/setores/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void rejeitaSiglaDuplicada() throws Exception {
        mvc.perform(post("/setores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Outro setor\",\"sigla\":\"COTIC\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void impedeExclusaoDeSetorComChamadoVinculado() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO chamado (titulo, descricao, local, prioridade, status, data_abertura,
                                     solicitante_id, categoria_id)
                VALUES ('Teste', 'Descrição', 'Sala', 'MEDIA', 'ABERTO', CURRENT_TIMESTAMP, 1, 1)
                """);

        mvc.perform(delete("/setores/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value("Não é possível excluir um setor com chamados vinculados."));
    }
}
