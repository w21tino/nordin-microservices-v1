package com.nordin.organization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Organization Service — Microservicio de organizaciones.
 *
 * Es el orquestador principal del dominio:
 *   organization-service
 *     └── department-service (Feign)
 *           └── employee-service (Feign interno de department)
 *
 * Cada llamada tiene su propio Circuit Breaker.
 * Si department-service falla, retorna departments vacío + message.
 * El cliente siempre recibe una respuesta — nunca un error 500
 * por fallo de un servicio downstream.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class OrganizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrganizationServiceApplication.class, args);
    }
}
