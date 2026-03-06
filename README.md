# nordin-microservices-v1

Arquitectura de microservicios con Spring Boot 3.3.x — Versión inicial.

## Stack Tecnológico

| Componente | Tecnología |
|---|---|
| Framework | Spring Boot 3.3.x |
| Registro | Eureka Server |
| Configuración | Spring Cloud Config + GitHub |
| Gateway | Spring Cloud Gateway (reactivo) |
| Comunicación | Feign Client |
| Resiliencia | Resilience4j |
| Trazabilidad | Micrometer Tracing + Zipkin |
| Métricas | Prometheus + Grafana |
| Monitoreo | Spring Boot Admin |
| Seguridad | JWT en Gateway |
| Base de datos | PostgreSQL (una por servicio) |
| Build | Maven multi-módulo |

## Puertos

| Servicio | Puerto |
|---|---|
| API Gateway | 8080 |
| Config Server | 8888 |
| Eureka Server | 8761 |
| Admin Server | 9090 |
| Organization Service | 8081 |
| Department Service | 8082 |
| Employee Service | 8083 |
| Zipkin | 9411 |
| Prometheus | 9090 |
| Grafana | 3000 |
| PostgreSQL org | 5432 |
| PostgreSQL dept | 5433 |
| PostgreSQL emp | 5434 |

## Orden de Arranque

```
1. docker-compose up (infraestructura)
2. Config Server
3. Eureka Server
4. Admin Server + API Gateway
5. Microservicios de negocio
```

## Arranque Rápido

```bash
# 1. Copiar variables de entorno
cp .env.example .env

# 2. Levantar infraestructura Docker
docker-compose up -d

# 3. Arrancar servicios en orden
# (ver documentación de cada módulo)
```

## Estructura del Proyecto

```
nordin-microservices-v1/
├── infrastructure/
│   ├── config-server/
│   ├── eureka-server/
│   ├── api-gateway/
│   └── admin-server/
├── services/
│   ├── organization-service/
│   ├── department-service/
│   └── employee-service/
├── docker/
│   ├── prometheus/
│   └── grafana/
├── docker-compose.yml
└── .env.example
```

## Repositorio de Configuración

Las configuraciones de cada servicio viven en un repositorio GitHub separado:
`nordin-config-repo`

## V2 — Migración a Kubernetes

Ver repositorio: `nordin-microservices-v2`
