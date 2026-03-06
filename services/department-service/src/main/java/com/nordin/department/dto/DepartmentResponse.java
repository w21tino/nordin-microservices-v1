package com.nordin.department.dto;

import java.util.List;
import java.util.UUID;

/**
 * DepartmentResponse — DTO de salida con empleados anidados.
 *
 * employees: lista de empleados obtenida de employee-service via Feign.
 *            Puede ser vacía si el Circuit Breaker está abierto.
 *
 * message: campo de degradación — indica al cliente el estado
 *          del servicio en el momento de la respuesta.
 *
 * Ejemplos de message:
 * - null: respuesta completa sin degradación
 * - "⚠️ Servicio de empleados no disponible temporalmente"
 * - "⚠️ Información parcial: empleados no disponibles"
 */
public record DepartmentResponse(
        UUID id,
        String name,
        UUID organizationId,
        List<EmployeeResponse> employees,
        String message
) {}
