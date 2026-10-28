package br.edu.ifrn.api_chamados_ifrn.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.ifrn.api_chamados_ifrn.dto.SetorRequest;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class ApiExceptionHandlerTest {

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void setorValidoPassaPelaValidacao() throws Exception {
        mvc.perform(post("/teste/setor").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Tecnologia da Informação\",\"sigla\":\"COTIC\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Tecnologia da Informação"))
                .andExpect(jsonPath("$.sigla").value("COTIC"));
    }

    @Test
    void campoObrigatorioVazioRetornaErro400Padronizado() throws Exception {
        mvc.perform(post("/teste/setor").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\" \" , \"sigla\":\"COTIC\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("nome: O nome é obrigatório."))
                .andExpect(jsonPath("$.caminho").value("/teste/setor"));
    }

    @Test
    void campoMaiorQueAColunaRetornaErro400() throws Exception {
        String nomeLongo = "a".repeat(256);
        mvc.perform(post("/teste/setor").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nomeLongo + "\",\"sigla\":\"COTIC\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("nome: O nome deve ter no máximo 255 caracteres."));
    }

    @Test
    void corpoMalformadoRetornaErro400Padronizado() throws Exception {
        mvc.perform(post("/teste/setor").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Corpo da requisição inválido."))
                .andExpect(jsonPath("$.caminho").value("/teste/setor"));
    }

    @Test
    void recursoInexistenteRetornaErro404Padronizado() throws Exception {
        mvc.perform(get("/teste/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Setor não encontrado."))
                .andExpect(jsonPath("$.caminho").value("/teste/nao-encontrado"));
    }

    @Test
    void violacaoDeRegraRetornaErro409Padronizado() throws Exception {
        mvc.perform(get("/teste/conflito"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value("Setor possui vínculos."))
                .andExpect(jsonPath("$.caminho").value("/teste/conflito"));
    }

    @RestController
    static class ControllerDeTeste {

        @PostMapping("/teste/setor")
        SetorRequest receber(@Valid @RequestBody SetorRequest setor) {
            return setor;
        }

        @GetMapping("/teste/nao-encontrado")
        void naoEncontrado() {
            throw new RecursoNaoEncontradoException("Setor não encontrado.");
        }

        @GetMapping("/teste/conflito")
        void conflito() {
            throw new RegraNegocioException("Setor possui vínculos.");
        }
    }
}
