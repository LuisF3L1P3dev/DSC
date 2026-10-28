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

@SpringBootTest(properties = "spring.jpa.open-in-view=false")
@AutoConfigureMockMvc
@Transactional
class CategoriaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void cadastraConsultaAtualizaEExcluiCategoriaSemChamados() throws Exception {
        MvcResult criada = mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Impressoras\",\"descricao\":\"Falhas de impressão\",\"setorId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Impressoras"))
                .andExpect(jsonPath("$.setorId").value(1))
                .andReturn();

        String caminho = criada.getResponse().getHeader("Location");
        mvc.perform(get(caminho))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Falhas de impressão"));
        mvc.perform(put(caminho).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Equipamentos\",\"descricao\":\"Equipamentos do setor\",\"setorId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Equipamentos"))
                .andExpect(jsonPath("$.setorId").value(2));
        mvc.perform(delete(caminho)).andExpect(status().isNoContent());
        mvc.perform(get(caminho)).andExpect(status().isNotFound());
    }

    @Test
    void combinaFiltrosEPaginacao() throws Exception {
        mvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tamanhoPagina").value(10))
                .andExpect(jsonPath("$.totalElementos").value(4));
        mvc.perform(get("/categorias").param("nome", "REDE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].nome").value("Rede"))
                .andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(get("/categorias").param("setorId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(get("/categorias").param("nome", "rede").param("setorId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(0));
        mvc.perform(get("/categorias").param("nome", "rEdE").param("setorId", "1").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].setorId").value(1))
                .andExpect(jsonPath("$.tamanhoPagina").value(50))
                .andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(get("/categorias").param("size", "1").param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].nome").value("Hidráulica"))
                .andExpect(jsonPath("$.totalPaginas").value(4));
    }

    @Test
    void listaCategoriasDeUmSetorEVerificaSeEleExiste() throws Exception {
        mvc.perform(get("/setores/1/categorias").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tamanhoPagina").value(1))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(2));
        mvc.perform(get("/setores/1/categorias").param("nome", "COMPUT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].nome").value("Computadores"))
                .andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(get("/setores/999999/categorias"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void rejeitaDadosInvalidosESetorInexistente() throws Exception {
        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"setorId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Rede\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Rede\",\"setorId\":0}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Rede\",\"setorId\":999999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void impedeExclusaoDeCategoriaComChamadoVinculado() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO chamado (titulo, descricao, local, prioridade, status, data_abertura,
                                     solicitante_id, categoria_id)
                VALUES ('Teste', 'Descrição', 'Sala', 'MEDIA', 'ABERTO', CURRENT_TIMESTAMP, 1, 1)
                """);

        mvc.perform(delete("/categorias/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value("Não é possível excluir uma categoria com chamados vinculados."));
    }
}
