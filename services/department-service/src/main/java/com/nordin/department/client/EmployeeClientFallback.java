package com.nordin.department.client;

import com.nordin.department.dto.EmployeeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * EmployeeClientFallback — Respuesta de degradación cuando
 * employee-service no está disponible.
 *
 * El Circuit Breaker de Resilience4j invoca este fallback cuando:
 * - employee-service no responde (timeout)
 * - employee-service retorna errores consecutivos (umbral de fallo)
 * - El circuito está abierto (estado OPEN)
 *
 * Retorna lista vacía — el DepartmentService detecta esto
 * y puebla el campo 'message' con el aviso de degradación.
 *
 * Patrón: Graceful Degradation — el sistema sigue funcionando
 * con información parcial en lugar de fallar completamente.
 */
@Component
@Slf4j
public class EmployeeClientFallback implements EmployeeClient {

    @Override
    public List<EmployeeResponse> getEmployeesByDepartmentId(UUID departmentId) {
        log.warn("⚠️ Fallback activado para employee-service — departmentId: {}", departmentId);
        return Collections.emptyList();
    }
}
