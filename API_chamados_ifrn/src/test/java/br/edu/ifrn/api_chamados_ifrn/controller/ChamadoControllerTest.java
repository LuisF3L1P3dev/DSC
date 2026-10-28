package br.edu.ifrn.api_chamados_ifrn.controller;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.jpa.open-in-view=false")
@AutoConfigureMockMvc
@Transactional
class ChamadoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void abreChamadoEDevolveOsValoresDefinidosPeloService() throws Exception {
        mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Falha na rede",
                                  "descricao": "Sem conexão no laboratório",
                                  "local": "Bloco A",
                                  "solicitanteId": 1,
                                  "categoriaId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/chamados/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.prioridade").value("MEDIA"))
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.dataAbertura").isNotEmpty())
                .andExpect(jsonPath("$.solicitanteId").value(1))
                .andExpect(jsonPath("$.categoriaId").value(1))
                .andExpect(jsonPath("$.tecnicoId").value(nullValue()))
                .andExpect(jsonPath("$.dataFechamento").value(nullValue()))
                .andExpect(jsonPath("$.solucao").value(nullValue()));
    }

    @Test
    void preservaPrioridadeInformadaNoCorpo() throws Exception {
        mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Projetor sem imagem",
                                  "descricao": "O projetor liga, mas não exibe imagem",
                                  "local": "Sala 2",
                                  "prioridade": "ALTA",
                                  "solicitanteId": 1,
                                  "categoriaId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.status").value("ABERTO"));
    }

    @Test
    void devolve400ParaDadosObrigatoriosInvalidos() throws Exception {
        mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": " ",
                                  "descricao": "Descrição válida",
                                  "local": "Bloco A",
                                  "solicitanteId": 1,
                                  "categoriaId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.caminho").value("/chamados"))
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("titulo")));
    }

    @Test
    void devolve409QuandoTecnicoTentaAbrirChamado() throws Exception {
        mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON)
                        .content(pedido(3, 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.caminho").value("/chamados"));
    }

    @Test
    void devolve404QuandoSolicitanteOuCategoriaNaoExistem() throws Exception {
        mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON)
                        .content(pedido(999999, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON)
                        .content(pedido(1, 999999)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private static String pedido(int solicitanteId, int categoriaId) {
        return """
                {
                  "titulo": "Falha na rede",
                  "descricao": "Sem conexão no laboratório",
                  "local": "Bloco A",
                  "solicitanteId": %d,
                  "categoriaId": %d
                }
                """.formatted(solicitanteId, categoriaId);
    }
}
