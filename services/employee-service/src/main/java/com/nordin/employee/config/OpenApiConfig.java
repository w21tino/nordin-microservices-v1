package com.nordin.employee.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenApiConfig — Configuración de Swagger/OpenAPI 3.
 *
 * Decisiones:
 * - Cada microservicio expone su propio spec en /v3/api-docs
 * - El Gateway agrega todos los specs en un único Swagger UI
 * - Se documenta el servidor local para desarrollo
 *
 * URLs importantes:
 * - Swagger UI:  http://localhost:8083/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8083/v3/api-docs
 *
 * El JSON es el que consume el Gateway para la agregación.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI employeeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Service API")
                        .description("Microservicio de gestión de empleados. " +
                                "Expone endpoints para crear y consultar empleados. " +
                                "Consumido internamente por department-service via Feign.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Nordin")
                                .email("nordin@example.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083")
                                .description("Servidor local — desarrollo")
                ));
    }
}
