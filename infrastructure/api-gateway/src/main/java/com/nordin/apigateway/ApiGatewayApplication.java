package com.nordin.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway — Único punto de entrada a la arquitectura.
 *
 * Responsabilidades:
 * - Enrutamiento dinámico via Eureka (lb://nombre-servicio)
 * - Validación de JWT (fase final V1)
 * - Balanceo de carga client-side
 * - Trazabilidad distribuida con Micrometer + Zipkin
 *
 * Corre sobre Netty (reactivo) — NO tiene Tomcat.
 * En V2 con K8s, este Gateway se reemplaza por un Ingress Controller.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
