# Exercícios Práticos e Questões Teóricas sobre Java

## Parte 1: Exercícios Práticos

### Exercício 1: Classe Base
Criar a classe `Produto` com os seguintes requisitos:
1. Atributos privados: `nome`, `preco` e `quantidade`.
2. Construtor para inicialização dos atributos.
3. Métodos *getters* para os atributos.
4. Método `vender(int qtd)` que reduza o estoque apenas se houver quantidade suficiente.
5. Sobrescrita do método `toString()` para exibir os dados do produto de forma legível.

---

### Desafio: Interface e Implementação
1. Criar a interface `Descontavel` contendo o método `aplicarDesconto(double pct)`.
2. Fazer com que a classe `Produto` implemente a interface `Descontavel`.

---

## Parte 2: Questões Teóricas

1. **Por que a tipagem estática do Java é uma vantagem em equipes grandes?**
    * **Resposta:** Ela detecta erros de tipo em tempo de compilação (antes de rodar o código), oferece autocomplete preciso nas IDEs e serve como documentação viva do código, evitando que desenvolvedores passem parâmetros incompatíveis ao integrar módulos feitos por outras pessoas.

2. **Qual a diferença entre JDK e JRE — e qual precisamos instalar?**
    * **Resposta:**
        * **JRE (Java Runtime Environment):** Contém apenas o necessário para **executar** aplicações Java (como a JVM e bibliotecas padrão).
        * **JDK (Java Development Kit):** É o kit completo para **desenvolver**, contendo a JRE mais ferramentas de desenvolvimento (como o compilador `javac`).
        * **Qual instalar:** Devemos instalar o **JDK**, pois ele inclui o ambiente de desenvolvimento completo.

3. **Por que declaramos atributos como `private` em vez de públicos?**
    * **Resposta:** Para garantir o **encapsulamento**: atributos privados impedem o acesso e a modificação direta de fora da classe, permitindo controlar regras de validação (através de métodos) e proteger a integridade dos dados internos do objeto.

4. **Quando usar classe abstrata e quando usar interface?**
    * **Resposta:**
        * **Classe Abstrata:** Quando existe uma relação direta de herança ("é um") com compartilhamento de código/estado (atributos e métodos já implementados) entre classes correlatas.
        * **Interface:** Quando você quer definir um contrato de comportamento ("faz um") comum a classes que podem não ter relação direta de parentesco, permitindo flexibilidade e herança múltipla de tipos.

5. **O que é polimorfismo, em uma frase?**
    * **Resposta:** Polimorfismo é a capacidade de um mesmo método se comportar de formas diferentes dependendo do objeto concreto que o invoca.