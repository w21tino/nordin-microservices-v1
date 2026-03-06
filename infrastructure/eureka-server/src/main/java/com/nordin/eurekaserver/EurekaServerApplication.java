package com.nordin.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server — Registro y descubrimiento de servicios.
 *
 * Todos los microservicios se registran aquí al arrancar.
 * El API Gateway y Feign Client usan Eureka para resolver
 * las instancias disponibles de cada servicio.
 *
 * En V2 con K8s, Eureka se reemplaza por el service discovery
 * nativo de Kubernetes (kube-dns + Services).
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
