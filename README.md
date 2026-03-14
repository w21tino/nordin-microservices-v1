# nordin-microservices-v1

> **Arquitectura de microservicios de nivel producción** construida con Spring Boot 3.3, Spring Cloud 2023 y Java 21.
> Demuestra patrones reales de la industria: Config centralizado, Service Discovery, Circuit Breaker en cascada, JWT en el Gateway y Observabilidad completa.

---

## Tabla de Contenidos

- [Visión General](#visión-general)
- [Stack Tecnológico](#stack-tecnológico)
- [Arquitectura del Sistema](#arquitectura-del-sistema)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Prerrequisitos](#prerrequisitos)
- [Arranque de la Infraestructura](#arranque-de-la-infraestructura)
- [Pruebas de la API](#pruebas-de-la-api)
- [Observabilidad](#observabilidad)
- [Patrones Implementados](#patrones-implementados)
- [Guía de Troubleshooting](#guía-de-troubleshooting)
- [Decisiones Arquitectónicas](#decisiones-arquitectónicas)

---

## Visión General

Este proyecto implementa un sistema de gestión organizacional distribuido con tres servicios de negocio interconectados. El enfoque principal es demostrar cómo se comporta una arquitectura de microservicios ante fallos reales, con degradación controlada en cada nivel de la cadena.

| Componente | Puerto | Responsabilidad |
|---|---|---|
| **config-server** | 8888 | Configuración centralizada desde GitHub |
| **eureka-server** | 8761 | Service Discovery y registro de servicios |
| **api-gateway** | 8080 | Único punto de entrada + validación JWT + Swagger agregado |
| **admin-server** | 9091 | Dashboard de monitorización en tiempo real |
| **organization-service** | 8081 | Orquestador principal → llama a department-service |
| **department-service** | 8082 | Gestión de departamentos → llama a employee-service |
| **employee-service** | 8083 | Servicio hoja — sin dependencias externas |

### Cadena de llamadas

```
Cliente
  └── API Gateway (JWT)
        ├── organization-service
        │     └── department-service (Feign + Circuit Breaker)
        │           └── employee-service (Feign + Circuit Breaker)
        ├── department-service
        └── employee-service
```

---

## Stack Tecnológico

| Categoría | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 21 |
| Framework | Spring Boot | 3.3.4 |
| Cloud | Spring Cloud | 2023.0.3 |
| Service Discovery | Netflix Eureka | — |
| Config | Spring Cloud Config | — |
| Gateway | Spring Cloud Gateway | — |
| Comunicación | OpenFeign | — |
| Resiliencia | Resilience4j | — |
| Persistencia | Spring Data JPA + PostgreSQL | — |
| Mapeo | MapStruct | — |
| Documentación | SpringDoc OpenAPI 3 | 2.5.0 |
| Trazabilidad | Micrometer Tracing + Zipkin | — |
| Métricas | Micrometer + Prometheus + Grafana | — |
| Monitorización | Spring Boot Admin | — |
| Seguridad | Spring Security + JJWT | 0.12.3 |
| Build | Maven (multi-módulo) | 3.9+ |
| Contenedores | Docker + Docker Compose | — |

---

## Arquitectura del Sistema

### Diagrama de componentes

```mermaid
graph TB
    Client([Cliente / Postman])

    subgraph "Infraestructura"
        GW[API Gateway\n:8080\nJWT + Routing]
        CFG[Config Server\n:8888\nGitHub]
        EUR[Eureka Server\n:8761\nService Discovery]
        ADM[Admin Server\n:9091\nDashboard]
    end

    subgraph "Servicios de Negocio"
        ORG[organization-service\n:8081]
        DEPT[department-service\n:8082]
        EMP[employee-service\n:8083]
    end

    subgraph "Bases de Datos"
        ODB[(org-db\n:5432)]
        DDB[(dept-db\n:5433)]
        EDB[(emp-db\n:5434)]
    end

    subgraph "Observabilidad"
        ZIP[Zipkin\n:9411]
        PROM[Prometheus\n:9090]
        GRAF[Grafana\n:3000]
    end

    Client -->|HTTP| GW
    GW -->|lb://| ORG
    GW -->|lb://| DEPT
    GW -->|lb://| EMP

    ORG -->|Feign + CB| DEPT
    DEPT -->|Feign + CB| EMP

    ORG --- ODB
    DEPT --- DDB
    EMP --- EDB

    CFG -.->|Configuración| ORG
    CFG -.->|Configuración| DEPT
    CFG -.->|Configuración| EMP
    CFG -.->|Configuración| GW

    EUR -.->|Registry| ORG
    EUR -.->|Registry| DEPT
    EUR -.->|Registry| EMP
    EUR -.->|Registry| GW

    ORG -.->|Trazas| ZIP
    DEPT -.->|Trazas| ZIP
    EMP -.->|Trazas| ZIP

    PROM -.->|Scrape| ORG
    PROM -.->|Scrape| DEPT
    PROM -.->|Scrape| EMP
    GRAF -.->|Query| PROM

    ADM -.->|Actuator| ORG
    ADM -.->|Actuator| DEPT
    ADM -.->|Actuator| EMP

    style GW fill:#4CAF50,color:#fff
    style ORG fill:#2196F3,color:#fff
    style DEPT fill:#2196F3,color:#fff
    style EMP fill:#2196F3,color:#fff
    style CFG fill:#FF9800,color:#fff
    style EUR fill:#FF9800,color:#fff
    style ZIP fill:#9C27B0,color:#fff
    style PROM fill:#9C27B0,color:#fff
    style GRAF fill:#9C27B0,color:#fff
    style ADM fill:#607D8B,color:#fff
```

### Flujo de una petición GET /organizations/{id}

```mermaid
sequenceDiagram
    participant C as Cliente
    participant GW as API Gateway
    participant ORG as organization-service
    participant DEPT as department-service
    participant EMP as employee-service
    participant ZIP as Zipkin

    C->>GW: GET /api/organizations/{id} Bearer JWT
    GW->>GW: Valida JWT
    GW->>ORG: GET /api/organizations/{id}
    activate ORG

    ORG->>DEPT: GET /api/departments/organization/{id}
    activate DEPT

    DEPT->>EMP: GET /api/employees/department/{deptId}
    activate EMP
    EMP-->>DEPT: [empleados]
    deactivate EMP

    DEPT-->>ORG: [departamentos con empleados]
    deactivate DEPT

    ORG-->>GW: org con departments y employees anidados
    deactivate ORG

    GW-->>C: 200 OK - Árbol completo

    ORG--)ZIP: Traza completa con spanIds
    DEPT--)ZIP: Span interno
    EMP--)ZIP: Span interno
```

### Comportamiento del Circuit Breaker

```mermaid
stateDiagram-v2
    [*] --> CLOSED: Estado inicial

    CLOSED --> OPEN: Tasa de fallos >= 50% en ventana de 10 llamadas

    OPEN --> HALF_OPEN: Después de 10s

    HALF_OPEN --> CLOSED: 3 llamadas exitosas
    HALF_OPEN --> OPEN: Cualquier fallo

    note right of CLOSED
        Operación normal
        Todas las llamadas pasan
        Se cuentan los fallos
    end note

    note right of OPEN
        Modo fallo rápido
        Fallback inmediato
        Sin llamadas al servicio caído
        El cliente siempre recibe 200
    end note

    note right of HALF_OPEN
        Modo prueba
        Sondeo controlado
        Evaluando recuperación
    end note
```

### Degradación en cascada

Cuando un servicio cae, el sistema responde de forma controlada:

```
┌─────────────────────────────────────────────────────────┐
│  employee-service DOWN                                   │
│                                                         │
│  department-service → fallback → employees: []          │
│                                  message: "⚠️ ..."     │
│                                                         │
│  organization-service → fallback → departments: []      │
│                                    message: "⚠️ ..."   │
│                                                         │
│  Cliente recibe → 200 OK (nunca un 500)                 │
└─────────────────────────────────────────────────────────┘
```

---

## Estructura del Proyecto

```
nordin-microservices-v1/
│
├── pom.xml                          ← POM padre multi-módulo
│
├── infrastructure/
│   ├── config-server/               ← Spring Cloud Config (lee de GitHub)
│   ├── eureka-server/               ← Netflix Eureka Server
│   ├── api-gateway/                 ← Spring Cloud Gateway + JWT + Swagger
│   └── admin-server/                ← Spring Boot Admin
│
├── services/
│   ├── organization-service/        ← Orquestador, llama a dept-service
│   │   ├── client/
│   │   │   ├── DepartmentClient.java           ← Feign Client
│   │   │   └── DepartmentResilienceClient.java ← Wrapper Circuit Breaker
│   │   ├── controller/
│   │   ├── service/
│   │   ├── model/, dto/, mapper/, exception/
│   │
│   ├── department-service/          ← Llama a employee-service
│   │   ├── client/
│   │   │   ├── EmployeeClient.java             ← Feign Client
│   │   │   └── EmployeeResilienceClient.java   ← Wrapper Circuit Breaker
│   │   ├── controller/
│   │   ├── service/
│   │   ├── model/, dto/, mapper/, exception/
│   │
│   └── employee-service/            ← Servicio hoja, sin dependencias
│       ├── controller/
│       ├── service/
│       ├── model/, dto/, mapper/, exception/
│
├── docker/
│   └── prometheus/prometheus.yml    ← Configuración de scraping
│
├── docker-compose.yml               ← PostgreSQL x3, Zipkin, Prometheus, Grafana
│
└── postman/
    └── nordin-microservices-v1.postman_collection.json
```

---

## Prerrequisitos

| Herramienta | Versión | Verificación |
|---|---|---|
| Java | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker + Docker Compose | Última | `docker --version` |

---

## Arranque de la Infraestructura

### Paso 1 — Levantar contenedores Docker

```bash
cd nordin-microservices-v1
docker-compose up -d
```

Verifica que todos los contenedores están healthy:

```bash
docker-compose ps
```

Resultado esperado:
```
org-db     Up (healthy)   0.0.0.0:5432->5432/tcp
dept-db    Up (healthy)   0.0.0.0:5433->5432/tcp
emp-db     Up (healthy)   0.0.0.0:5434->5432/tcp
zipkin     Up (healthy)   0.0.0.0:9411->9411/tcp
prometheus Up             0.0.0.0:9090->9090/tcp
grafana    Up             0.0.0.0:3000->3000/tcp
```

### Paso 2 — Arrancar servicios en orden

```bash
# Terminal 1 — Config Server (primero siempre)
cd infrastructure/config-server && mvn spring-boot:run

# Terminal 2 — Eureka Server
cd infrastructure/eureka-server && mvn spring-boot:run

# Terminal 3 — Admin Server
cd infrastructure/admin-server && mvn spring-boot:run

# Terminal 4 — API Gateway
cd infrastructure/api-gateway && mvn spring-boot:run

# Terminal 5 — Employee Service (servicio hoja)
cd services/employee-service && mvn spring-boot:run

# Terminal 6 — Department Service
cd services/department-service && mvn spring-boot:run

# Terminal 7 — Organization Service
cd services/organization-service && mvn spring-boot:run
```

### Paso 3 — Verificar en Eureka

Abre `http://localhost:8761` (usuario: `eureka-user`, contraseña: `eureka-pass`).

Deben aparecer los 7 servicios registrados como UP.

---

## Pruebas de la API

### Crear datos de prueba

```bash
# 1. Crear organización
curl -s -X POST http://localhost:8081/api/organizations \
  -H "Content-Type: application/json" \
  -d '{"name": "Nordin Corp", "address": "Calle Principal 123"}' | python3 -m json.tool

# 2. Crear departamento (reemplaza ORG_ID con el id retornado)
curl -s -X POST http://localhost:8082/api/departments \
  -H "Content-Type: application/json" \
  -d '{"name": "Ingeniería", "organizationId": "ORG_ID"}' | python3 -m json.tool

# 3. Crear empleado (reemplaza DEPT_ID)
curl -s -X POST http://localhost:8083/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name": "Juan Pérez", "email": "juan@nordin.com", "departmentId": "DEPT_ID"}' | python3 -m json.tool
```

### Consultar el árbol completo

```bash
curl -s http://localhost:8081/api/organizations/ORG_ID | python3 -m json.tool
```

Respuesta esperada con todos los servicios UP:

```json
{
  "id": "...",
  "name": "Nordin Corp",
  "departments": [
    {
      "name": "Ingeniería",
      "employees": [{ "name": "Juan Pérez", "email": "juan@nordin.com" }],
      "message": null
    }
  ],
  "message": null
}
```

`message: null` indica que todos los servicios respondieron correctamente sin degradación.

### Probar Circuit Breaker

```bash
# 1. Detener employee-service (Ctrl+C en su terminal)

# 2. Consultar la organización
curl -s http://localhost:8081/api/organizations/ORG_ID | python3 -m json.tool
```

Respuesta con degradación controlada — siempre 200 OK:

```json
{
  "id": "...",
  "name": "Nordin Corp",
  "departments": [],
  "message": "⚠️ Información parcial: servicio de departamentos no disponible temporalmente"
}
```

### Verificar estado del Circuit Breaker

```bash
curl -s http://localhost:8082/actuator/circuitbreakers | python3 -m json.tool
```

---

## Observabilidad

| Herramienta | URL | Credenciales |
|---|---|---|
| **Eureka Dashboard** | http://localhost:8761 | eureka-user / eureka-pass |
| **Swagger UI agregado** | http://localhost:8080/swagger-ui.html | — |
| **Admin Server** | http://localhost:9091 | admin / admin |
| **Zipkin** | http://localhost:9411 | — |
| **Prometheus** | http://localhost:9090 | — |
| **Grafana** | http://localhost:3000 | admin / admin |

### Zipkin — Trazabilidad distribuida

Después de hacer peticiones, abre `http://localhost:9411` → **Run Query**. Cada traza muestra la cadena completa:

```
organization-service  159ms  (orquesta todo)
  department-service   50ms  (primer departamento)
    employee-service   19ms  (empleados del dept)
  department-service   60ms  (segundo departamento)
    employee-service   27ms  (empleados del dept)
```

Todos los spans de la misma petición comparten el mismo `traceId`.

### Grafana — Métricas históricas

1. Connections → Data Sources → Prometheus → URL: `http://prometheus:9090`
2. Dashboards → Import → ID: `11378` (Spring Boot 3.x dashboard oficial)
3. Selecciona cada servicio en el dropdown **Application**

### Admin Server — Tiempo real

En `http://localhost:9091` puedes cambiar el nivel de log de cualquier servicio en caliente sin reiniciar:

```
Loggers → com.nordin.department → DEBUG
```

---

## Patrones Implementados

### Config Server centralizado

Todos los microservicios obtienen su configuración de GitHub al arrancar vía Config Server. Cambiar configuración no requiere redeployar.

### Service Discovery con Eureka

Sin URLs hardcodeadas. Los Feign Clients y el Gateway resuelven los servicios por nombre:

```java
@FeignClient(name = "department-service", fallback = DepartmentClientFallback.class)
public interface DepartmentClient {
    @GetMapping("/api/departments/organization/{organizationId}")
    List<DepartmentResponse> getDepartmentsByOrganizationId(@PathVariable UUID organizationId);
}
```

### Circuit Breaker — Patrón Wrapper

El proxy AOP de Resilience4j no intercepta llamadas dentro del mismo bean. La solución es un componente separado:

```java
@Component
public class EmployeeResilienceClient {

    @CircuitBreaker(name = "employee-service", fallbackMethod = "employeesFallback")
    @Retry(name = "employee-service")
    public List<EmployeeResponse> getEmployeesWithResilience(UUID departmentId) {
        return employeeClient.getEmployeesByDepartmentId(departmentId);
    }

    public List<EmployeeResponse> employeesFallback(UUID departmentId, Throwable t) {
        log.warn("Fallback activado para dept {}: {}", departmentId, t.getMessage());
        return Collections.emptyList();
    }
}
```

`@Retry` envuelve a `@CircuitBreaker`: primero reintenta 3 veces con 1s de espera y si agota los reintentos, el Circuit Breaker contabiliza el fallo.

### Edge Security con JWT

El JWT se valida una sola vez en el Gateway. Los microservicios internos reciben el `userId` como header `X-User-Id`:

```
Cliente → [JWT válido] → Gateway → [X-User-Id header] → Microservicios
```

### Swagger agregado en el Gateway

Un único Swagger UI en `http://localhost:8080/swagger-ui.html` consolida los 3 microservicios via rutas `/aggregate/{service}/v3/api-docs`.

---

## Guía de Troubleshooting

### Puerto ocupado al arrancar

```bash
sudo lsof -ti:8888 | xargs kill -9
```

**Caso Admin Server:** Prometheus ocupa el 9090. Cambiar `server.port` a `9091` en `admin-server/application.yml`.

---

### Servicio no aparece en Eureka

Los mensajes `Connection refused to Eureka` durante el arranque son normales. El cliente reintenta cada 30 segundos automáticamente. Verificar:

```bash
curl -s http://eureka-user:eureka-pass@localhost:8761/actuator/health
```

---

### Circuit Breaker no activa el fallback

Causa: llamada Feign dentro de una lambda/stream del mismo bean — AOP no puede interceptar. Solución: extraer a un `@Component` separado (patrón Wrapper descrito arriba).

---

### Prometheus no scrapea en Linux

`host.docker.internal` no existe en Linux. Usar la IP del gateway Docker:

```bash
docker network inspect bridge | grep Gateway
# "Gateway": "172.17.0.1"
```

Reemplazar en `prometheus.yml` y reiniciar: `docker restart prometheus`

---

### Config Server o Eureka retornan 401 en Prometheus

Agregar `basic_auth` en `prometheus.yml`:

```yaml
- job_name: 'config-server'
  basic_auth:
    username: config-user
    password: config-pass
  static_configs:
    - targets: ['172.17.0.1:8888']
```

---

### Swagger UI pide usuario y contraseña

Spring Security intercepta antes que el filtro JWT. Crear `SecurityConfig` con `httpBasic.disable()` y `formLogin.disable()` que permita las rutas `/swagger-ui/**`, `/webjars/**`, `/aggregate/**`.

---

## Decisiones Arquitectónicas

### ADR-001 — Maven multi-módulo con POM padre

**Decisión:** Un único `pom.xml` raíz gestiona versiones de Spring Boot, Spring Cloud y Java para los 7 módulos.

**Consecuencias:** Versiones actualizadas en un único lugar. Build completo con `mvn clean install` desde la raíz.

---

### ADR-002 — PostgreSQL independiente por servicio

**Decisión:** Tres instancias PostgreSQL en Docker (puertos 5432, 5433, 5434), una por servicio de negocio.

**Consecuencias:** Aislamiento total — un servicio no puede acceder a la base de datos de otro. Refleja fielmente un entorno productivo aunque aumenta el consumo de recursos en desarrollo.

---

### ADR-003 — Feign + Eureka para comunicación inter-servicio

**Decisión:** OpenFeign con resolución de nombres via Eureka. Sin URLs hardcodeadas en el código.

**Alternativas descartadas:** RestTemplate (más verboso), WebClient (reactivo, innecesario para servicios síncronos).

---

### ADR-004 — Patrón Wrapper para Circuit Breaker

**Problema:** Resilience4j usa proxies AOP. Si la llamada Feign ocurre dentro de un stream del mismo bean, el proxy no la intercepta y el Circuit Breaker no se activa.

**Decisión:** Extraer la llamada Feign a un `@Component` separado (`EmployeeResilienceClient`, `DepartmentResilienceClient`). La llamada cruza el límite del bean y el proxy AOP la intercepta correctamente.

---

### ADR-005 — Edge Security: JWT solo en el Gateway

**Decisión:** El JWT se valida únicamente en el API Gateway. Los microservicios internos reciben el `userId` como header y confían en el Gateway.

**Consecuencias:** La lógica de seguridad no se duplica. Cambios en el algoritmo JWT solo afectan al Gateway. Los microservicios son más simples y rápidos.

---

### ADR-006 — Logback con perfiles dev/prod

**Decisión:** `logback-spring.xml` con dos appenders activados por perfil: `dev` usa ConsoleAppender con colores, `prod` usa JsonEncoder para ingestión por ELK/Datadog.

**Consecuencias:** El mismo JAR se comporta diferente según el perfil. No requiere cambios de código.

---

## Repositorios

| Repositorio | Descripción |
|---|---|
| [nordin-microservices-v1](https://github.com/w21tino/nordin-microservices-v1) | Código fuente completo |
| [nordin-config-repo](https://github.com/w21tino/nordin-config-repo) | Configuraciones centralizadas |
