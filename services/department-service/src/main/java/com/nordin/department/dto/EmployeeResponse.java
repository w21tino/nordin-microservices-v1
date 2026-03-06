package com.nordin.department.dto;

import java.util.UUID;

/**
 * EmployeeResponse — DTO que representa la respuesta de employee-service.
 *
 * Este DTO replica el contrato de employee-service.
 * En microservicios cada servicio define su propia copia del DTO
 * del servicio que consume — NO se comparte código entre servicios.
 * Esto mantiene el desacoplamiento entre módulos.
 */
public record EmployeeResponse(
        UUID id,
        String name,
        String email,
        UUID departmentId
) {}
