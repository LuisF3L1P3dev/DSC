package br.edu.ifrn;

import br.edu.ifrn.service.ProdutoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;

@SpringBootApplication
public class PrecoApplication implements CommandLineRunner {

    private final ProdutoService produtoService;

    public PrecoApplication(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    public static void main(String[] args) {
        SpringApplication.run(PrecoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        double custo = 300.00;
        String localEntrega = "SC";

        double precoFinal = produtoService.calcularPrecoFinal(custo, localEntrega);

        System.out.printf(
                Locale.forLanguageTag("pt-BR"),
                "Preço final: R$ %.2f%n",
                precoFinal
        );
    }
}
