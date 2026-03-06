package com.nordin.department.client;

import com.nordin.department.dto.EmployeeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

/**
 * EmployeeClient — Feign Client para comunicación con employee-service.
 *
 * Decisiones clave:
 * - name = "employee-service": Feign resuelve este nombre via Eureka.
 *   Spring Cloud LoadBalancer selecciona la instancia disponible.
 *   Si hay múltiples instancias registradas, balancea automáticamente.
 *
 * - fallback = EmployeeClientFallback.class: cuando el Circuit Breaker
 *   está abierto o hay timeout, Resilience4j invoca el fallback
 *   en lugar de propagar el error al cliente.
 *
 * Por qué Feign sobre RestTemplate:
 * - Declara el contrato como interfaz — legible y mantenible
 * - Integración nativa con Eureka, Resilience4j y Micrometer
 * - En V2 con K8s, solo cambia la URL — el código no cambia
 */
@FeignClient(
        name = "employee-service",
        fallback = EmployeeClientFallback.class
)
public interface EmployeeClient {

    @GetMapping("/api/employees/department/{departmentId}")
    List<EmployeeResponse> getEmployeesByDepartmentId(@PathVariable UUID departmentId);
}
