package br.edu.ifrn.calculadora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.ifrn.calculadora.controllers.CalculadoraController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

// Carrega somente a parte web necessária para testar o controller.
@WebMvcTest(CalculadoraController.class)
class CalculadoraControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveSomarNumerosRecebidosPeloCaminho() throws Exception {
        mockMvc.perform(get("/calculadora/somar/10/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("15"));
    }

    @Test
    void deveSubtrairNumerosRecebidosPorParametros() throws Exception {
        mockMvc.perform(get("/calculadora/subtrair")
                        .param("numero1", "20")
                        .param("numero2", "8"))
                .andExpect(status().isOk())
                .andExpect(content().string("12"));
    }

    @Test
    void deveCalcularEUsarDuasCasasDecimaisPorPadrao() throws Exception {
        mockMvc.perform(get("/calculadora/calcular/multiplicar")
                        .param("n1", "10")
                        .param("n2", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Operação: multiplicação\n"
                                + "Número 1: 10\n"
                                + "Número 2: 5\n"
                                + "Resultado: 50.00"));
    }

    @Test
    void deveRespeitarQuantidadeDeCasasDecimais() throws Exception {
        mockMvc.perform(get("/calculadora/calcular/dividir")
                        .param("n1", "10")
                        .param("n2", "3")
                        .param("casasDecimais", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Operação: divisão\n"
                                + "Número 1: 10\n"
                                + "Número 2: 3\n"
                                + "Resultado: 3.333"));
    }

    @Test
    void deveRecusarDivisaoPorZero() throws Exception {
        mockMvc.perform(get("/calculadora/calcular/dividir")
                        .param("n1", "10")
                        .param("n2", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string("Erro: não é possível dividir por zero."));
    }

    @Test
    void deveRecusarOperacaoDesconhecida() throws Exception {
        mockMvc.perform(get("/calculadora/calcular/potencia")
                        .param("n1", "10")
                        .param("n2", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Erro: operação inválida. Use somar, subtrair, multiplicar ou dividir."));
    }

    @Test
    void deveInformarSeNumeroEImpar() throws Exception {
        mockMvc.perform(get("/calculadora/par-ou-impar/7"))
                .andExpect(status().isOk())
                .andExpect(content().string("O número 7 é ÍMPAR."));
    }

    @Test
    void deveAnalisarNumero() throws Exception {
        mockMvc.perform(get("/calculadora/analisar/10"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Número: 10\n"
                                + "Par ou ímpar: PAR\n"
                                + "Positivo, negativo ou zero: POSITIVO\n"
                                + "Dobro: 20\n"
                                + "Metade: 5\n"
                                + "Quadrado: 100"));
    }

    @ParameterizedTest
    @CsvSource({
            "7, 8, 6, 'Média: 7.0|Situação: APROVADO'",
            "4, 4, 4, 'Média: 4.0|Situação: RECUPERAÇÃO'",
            "3, 3, 3, 'Média: 3.0|Situação: REPROVADO'"
    })
    void deveClassificarMedia(
            String nota1,
            String nota2,
            String nota3,
            String respostaEsperada) throws Exception {

        // O caractere | facilita escrever a quebra de linha dentro da tabela de exemplos.
        respostaEsperada = respostaEsperada.replace('|', '\n');

        mockMvc.perform(get("/calculadora/media")
                        .param("nota1", nota1)
                        .param("nota2", nota2)
                        .param("nota3", nota3))
                .andExpect(status().isOk())
                .andExpect(content().string(respostaEsperada));
    }

    @Test
    void deveExigirTodosOsParametrosDaMedia() throws Exception {
        mockMvc.perform(get("/calculadora/media")
                        .param("nota1", "7")
                        .param("nota2", "8"))
                .andExpect(status().isBadRequest());
    }
}
