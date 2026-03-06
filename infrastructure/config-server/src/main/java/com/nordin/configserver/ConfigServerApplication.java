package com.nordin.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server — Servidor de configuración centralizada.
 *
 * Todos los microservicios obtienen su configuración desde aquí.
 * El backend de configuración es un repositorio GitHub.
 *
 * Protegido con autenticación básica (usuario/contraseña).
 * En V2 con K8s, esto se reemplaza por Kubernetes Secrets + ConfigMaps.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
