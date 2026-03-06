package com.nordin.department;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Department Service — Microservicio de departamentos.
 *
 * Responsabilidades:
 * - Gestión de departamentos (GET, POST)
 * - Llama a employee-service via Feign para obtener empleados
 * - Implementa Circuit Breaker con Resilience4j
 * - El campo 'message' indica degradación del servicio
 *
 * Flujo: API Gateway → department-service → dept-db
 *                                        → employee-service (Feign)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class DepartmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DepartmentServiceApplication.class, args);
    }
}
