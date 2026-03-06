package com.nordin.organization.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI organizationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Organization Service API")
                        .description("Microservicio de organizaciones. Orquesta llamadas a " +
                                "department-service (que a su vez llama a employee-service). " +
                                "Implementa Circuit Breaker en cada nivel de la cadena.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Nordin")
                                .email("nordin@example.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Servidor local — desarrollo")
                ));
    }
}
