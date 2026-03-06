package com.nordin.adminserver;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Admin Server — Monitoreo visual de todos los microservicios.
 *
 * Se conecta a Eureka para descubrir automáticamente
 * todos los servicios registrados y mostrar su estado,
 * métricas, logs y health checks en tiempo real.
 *
 * Dashboard disponible en: http://localhost:9090
 */
@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient
public class AdminServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServerApplication.class, args);
    }
}
