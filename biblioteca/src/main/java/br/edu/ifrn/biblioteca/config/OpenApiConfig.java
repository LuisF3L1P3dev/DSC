package br.edu.ifrn.biblioteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI bibliotecaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API do Sistema de Biblioteca")
                        .description("Rotas de consulta do exercício de Spring Data JPA")
                        .version("1.0.0"));
    }
}
