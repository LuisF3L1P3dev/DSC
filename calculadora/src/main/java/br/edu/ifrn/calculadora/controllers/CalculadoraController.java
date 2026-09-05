package br.edu.ifrn.calculadora.controllers;

import java.util.Locale;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// recebe requisições HTTP e resultado no corpo da resposta.
@RestController
// início de todas as URLs deste controller.
@RequestMapping("/calculadora")
public class CalculadoraController {

    @GetMapping("/somar/{numero1}/{numero2}")
    public int somar(@PathVariable int numero1, @PathVariable int numero2) {
        // Os valores entre chaves na URL chegam ao método por meio de @PathVariable.
        return numero1 + numero2;
    }

    @GetMapping("/subtrair")
    public int subtrair(@RequestParam int numero1, @RequestParam int numero2) {
        // @RequestParam recebe valores escritos após o sinal de interrogação da URL.
        return numero1 - numero2;
    }

    @GetMapping("/calcular/{operacao}")
    public String calcular(
            @PathVariable String operacao,
            @RequestParam double n1,
            @RequestParam double n2,
            @RequestParam(defaultValue = "2") int casasDecimais) {

        // Limitamos a precisão para evitar valores negativos ou respostas exageradamente grandes.
        if (casasDecimais < 0 || casasDecimais > 10) {
            return "Erro: casasDecimais deve estar entre 0 e 10.";
        }

        double resultado;
        String nomeOperacao;

        // Um único switch permite atender as quatro operações sem repetir endpoints parecidos.
        switch (operacao.toLowerCase(Locale.ROOT)) {
            case "somar" -> {
                resultado = n1 + n2;
                nomeOperacao = "soma";
            }
            case "subtrair" -> {
                resultado = n1 - n2;
                nomeOperacao = "subtração";
            }
            case "multiplicar" -> {
                resultado = n1 * n2;
                nomeOperacao = "multiplicação";
            }
            case "dividir" -> {
                // A divisão por zero não produz um resultado matemático válido neste exercício.
                if (n2 == 0) {
                    return "Erro: não é possível dividir por zero.";
                }
                resultado = n1 / n2;
                nomeOperacao = "divisão";
            }
            default -> {
                return "Erro: operação inválida. Use somar, subtrair, multiplicar ou dividir.";
            }
        }

        // Locale.US garante o ponto como separador decimal, como nas URLs e nos exemplos.
        String resultadoFormatado = String.format(
                Locale.US,
                "%." + casasDecimais + "f",
                resultado);

        return "Operação: " + nomeOperacao
                + "\nNúmero 1: " + formatarNumero(n1)
                + "\nNúmero 2: " + formatarNumero(n2)
                + "\nResultado: " + resultadoFormatado;
    }

    @GetMapping("/par-ou-impar/{numero}")
    public String verificarParOuImpar(@PathVariable int numero) {
        // Se o resto da divisão por 2 for zero, o número é par.
        if (numero % 2 == 0) {
            return "O número " + numero + " é PAR.";
        }

        return "O número " + numero + " é ÍMPAR.";
    }

    @GetMapping("/analisar/{numero}")
    public String analisar(@PathVariable int numero) {
        String paridade = numero % 2 == 0 ? "PAR" : "ÍMPAR";
        String sinal;

        // A ordem das condições separa as três possibilidades pedidas no enunciado.
        if (numero > 0) {
            sinal = "POSITIVO";
        } else if (numero < 0) {
            sinal = "NEGATIVO";
        } else {
            sinal = "ZERO";
        }

        long dobro = (long) numero * 2;
        double metade = numero / 2.0;
        long quadrado = (long) numero * numero;

        return "Número: " + numero
                + "\nPar ou ímpar: " + paridade
                + "\nPositivo, negativo ou zero: " + sinal
                + "\nDobro: " + dobro
                + "\nMetade: " + formatarNumero(metade)
                + "\nQuadrado: " + quadrado;
    }

    private String formatarNumero(double numero) {
        // Evita mostrar ".0" quando o valor recebido ou calculado for inteiro.
        if (numero == Math.rint(numero)) {
            return String.format(Locale.US, "%.0f", numero);
        }

        return Double.toString(numero);
    }
}
