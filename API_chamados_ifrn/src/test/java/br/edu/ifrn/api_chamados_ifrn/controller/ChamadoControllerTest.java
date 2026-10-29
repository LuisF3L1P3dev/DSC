package br.edu.ifrn.api_chamados_ifrn.controller;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@SpringBootTest(properties = "spring.jpa.open-in-view=false")
@AutoConfigureMockMvc
@Transactional
class ChamadoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @Test
    void listaChamadosComFiltrosCombinadosDatasEPaginacao() throws Exception {
        inserirChamado("Rede urgente", "ALTA", "ABERTO",
                LocalDateTime.of(2026, 1, 5, 10, 0), 1L, 1L, 3L);
        inserirChamado("Manutenção agendada", "MEDIA", "EM_ATENDIMENTO",
                LocalDateTime.of(2026, 1, 10, 10, 0), 1L, 3L, 4L);
        inserirChamado("Computador cancelado", "BAIXA", "CANCELADO",
                LocalDateTime.of(2026, 2, 1, 10, 0), 2L, 2L, null);
        inserirChamado("Rede solicitante", "MEDIA", "ABERTO",
                LocalDateTime.of(2026, 3, 1, 10, 0), 2L, 1L, 3L);

        mvc.perform(get("/chamados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tamanhoPagina").value(10))
                .andExpect(jsonPath("$.totalElementos").value(4))
                .andExpect(jsonPath("$.itens[0].titulo").value("Rede solicitante"));
        mvc.perform(get("/chamados")
                        .param("status", "ABERTO")
                        .param("prioridade", "ALTA")
                        .param("setorId", "1")
                        .param("categoriaId", "1")
                        .param("solicitanteId", "1")
                        .param("tecnicoId", "3")
                        .param("dataInicio", "2026-01-01T00:00:00")
                        .param("dataFim", "2026-01-31T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].titulo").value("Rede urgente"))
                .andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(get("/chamados")
                        .param("dataInicio", "2026-01-10T10:00:00")
                        .param("dataFim", "2026-02-01T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(get("/chamados").param("size", "100").param("sort", "dataAbertura,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tamanhoPagina").value(50))
                .andExpect(jsonPath("$.totalElementos").value(4));
    }

    @Test
    void retornaDetalheComHistoricoDeInteracoes() throws Exception {
        Long chamadoId = inserirChamado("Falha na rede", "MEDIA", "ABERTO",
                LocalDateTime.of(2026, 1, 1, 9, 0), 1L, 1L, null);
        inserirInteracao("Técnico respondeu", LocalDateTime.of(2026, 1, 2, 10, 0), chamadoId, 3L);
        inserirInteracao("Solicitante relatou o problema", LocalDateTime.of(2026, 1, 1, 10, 0), chamadoId, 1L);

        mvc.perform(get("/chamados/{id}", chamadoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chamado.id").value(chamadoId))
                .andExpect(jsonPath("$.interacoes[0].texto").value("Solicitante relatou o problema"))
                .andExpect(jsonPath("$.interacoes[0].autorId").value(1))
                .andExpect(jsonPath("$.interacoes[1].texto").value("Técnico respondeu"));
        mvc.perform(get("/chamados/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listaChamadosDoSolicitanteOuDoTecnicoComFiltroDeStatus() throws Exception {
        inserirChamado("Rede da Ana", "ALTA", "ABERTO",
                LocalDateTime.of(2026, 1, 1, 9, 0), 1L, 1L, 3L);
        inserirChamado("Manutenção da Ana", "MEDIA", "RESOLVIDO",
                LocalDateTime.of(2026, 1, 2, 9, 0), 1L, 3L, 4L);
        inserirChamado("Rede do Bruno", "BAIXA", "ABERTO",
                LocalDateTime.of(2026, 1, 3, 9, 0), 2L, 1L, 3L);

        mvc.perform(get("/usuarios/1/chamados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tamanhoPagina").value(10))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.itens[0].titulo").value("Manutenção da Ana"));
        mvc.perform(get("/usuarios/1/chamados").param("status", "ABERTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(get("/usuarios/3/chamados").param("status", "ABERTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(get("/usuarios/999999/chamados"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void rejeitaIdsDeFiltroInvalidosEPeriodoInvertido() throws Exception {
        mvc.perform(get("/chamados").param("setorId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mvc.perform(get("/chamados")
                        .param("dataInicio", "2026-02-01T00:00:00")
                        .param("dataFim", "2026-01-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
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

    private Long inserirChamado(String titulo, String prioridade, String status, LocalDateTime dataAbertura,
            Long solicitanteId, Long categoriaId, Long tecnicoId) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO chamado (titulo, descricao, local, prioridade, status, data_abertura,
                                     solicitante_id, categoria_id, tecnico_id)
                VALUES (?, 'Descrição de teste', 'Sala 1', ?, ?, ?, ?, ?, ?)
                RETURNING id
                """, Long.class, titulo, prioridade, status, Timestamp.valueOf(dataAbertura),
                solicitanteId, categoriaId, tecnicoId);
    }

    private void inserirInteracao(String texto, LocalDateTime dataHora, Long chamadoId, Long autorId) {
        jdbcTemplate.update("""
                INSERT INTO interacao (texto, data_hora, chamado_id, autor_id)
                VALUES (?, ?, ?, ?)
                """, texto, Timestamp.valueOf(dataHora), chamadoId, autorId);
    }
}
