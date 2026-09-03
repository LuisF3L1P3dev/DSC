IFRN — Instituto Federal de Educação, Ciência e Tecnologia do Rio Grande do Norte Curso Superior de Tecnologia em Análise e Desenvolvimento de Sistemas

## Exercício — API de Calculadora com Spring Boot

Desenvolvimento de Sistemas Corporativos · 2026.2 · Prof. Jeferson Queiroga

Crie um CalculadoraController responsável por realizar operações matemáticas através de endpoints HTTP. Não utilize banco de dados, entidades ou camada de serviço. Toda a lógica deve ficar no controller.

Entrega. Suba o projeto em um repositório no GitHub e cole apenas o link do repositório na tarefa criada no Google Sala de Aula. Não é necessário anexar arquivos.

## O que será exercitado

Esta questão trabalha, em conjunto: @RestController, @RequestMapping, @GetMapping, @PathVariable, @RequestParam, parâmetros obrigatórios e opcionais (defaultValue), operadores matemáticos, estruturas condicionais (if/else, switch) e tratamento de situações inválidas.

## 1. Endpoint de soma com @PathVariable

GET /calculadora/somar/{numero1}/{numero2} — receba os dois números utilizando @PathVariable e retorne a soma.

GET /calculadora/somar/10/5

Resultado:

15

## 2. Endpoint de subtração com @RequestParam

GET /calculadora/subtrair?numero1=20&numero2=8 — receba os dois números utilizando @RequestParam e retorne a subtração.

## 3. Endpoint único de cálculo

GET /calculadora/calcular/{operacao}?numero1=10&numero2=5 — crie um único endpoint capaz de realizar as operações somar, subtrair, multiplicar e dividir. A operação deve ser recebida por @PathVariable e os números por @RequestParam.

/calculadora/calcular/somar?numero1=10&numero2=5

/calculadora/calcular/subtrair?numero1=10&numero2=5

/calculadora/calcular/multiplicar?numero1=10&numero2=5

/calculadora/calcular/dividir?numero1=10&numero2=5

O endpoint deve retornar uma mensagem informando a operação realizada e o resultado. Exemplo:

Operação: multiplicação

Número 1: 10

Número 2: 5

Resultado: 50

Divisão por zero. Trate a tentativa de divisão por zero — por exemplo, /calculadora/calcular/dividir?numero1=10&numero2=0 deve retornar algo como:

Erro: não é possível dividir por zero.


Casas decimais (parâmetro opcional). Acrescente um parâmetro opcional chamado casasDecimais, usando @RequestParam(defaultValue = "2"), de modo que, quando o usuário não informar casasDecimais, o sistema utilize 2.

/calculadora/calcular/dividir?numero1=10&numero2=3&casasDecimais=2

## 4. Par ou ímpar

GET /calculadora/par-ou-impar/{numero} — o endpoint deve informar se o número recebido é PAR ou ÍMPAR.

## 5. Análise de número

GET /calculadora/analisar/{numero} — deve retornar várias informações sobre o número recebido:

```
Número: 10
Par ou ímpar: PAR
Positivo, negativo ou zero: POSITIVO
Dobro: 20
Metade: 5
Quadrado: 100
```

## Desafio adicional — cálculo de média

GET /calculadora/media — deve receber três notas por @RequestParam:

/calculadora/media?nota1=7&nota2=8&nota3=6

## e retornar:

```
Média: 7.0
Situação: APROVADO
```

Considere as faixas: Média >= 7 APROVADO · Média >= 4 RECUPERAÇÃO · Média < 4 REPROVADO.

O endpoint /calculadora/calcular/{operacao} é a parte principal da avaliação: ele exige entender que a URL pode carregar parte da informação no caminho e outra parte nos parâmetros da requisição, em vez de criar quatro métodos quase idênticos.
