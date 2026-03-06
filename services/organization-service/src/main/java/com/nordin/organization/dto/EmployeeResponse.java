package com.nordin.organization.dto;

import java.util.UUID;

/**
 * EmployeeResponse — Replica del contrato de employee-service.
 * Cada servicio mantiene su propia copia — no se comparte código.
 */
public record EmployeeResponse(
        UUID id,
        String name,
        String email,
        UUID departmentId
) {}
