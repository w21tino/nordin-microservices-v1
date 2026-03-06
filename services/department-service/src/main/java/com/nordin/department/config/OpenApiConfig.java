package com.nordin.department.config;

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
    public OpenAPI departmentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Department Service API")
                        .description("Microservicio de departamentos. Orquesta llamadas a " +
                                "employee-service via Feign con Circuit Breaker (Resilience4j). " +
                                "El campo 'message' indica degradación del servicio.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Nordin")
                                .email("nordin@example.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Servidor local — desarrollo")
                ));
    }
}
