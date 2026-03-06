package com.nordin.organization.dto;

import java.util.List;
import java.util.UUID;

/**
 * DepartmentResponse — Replica del contrato de department-service.
 *
 * message: si department-service retorna este campo con valor,
 * significa que employee-service estaba degradado cuando
 * department-service procesó la solicitud.
 */
public record DepartmentResponse(
        UUID id,
        String name,
        UUID organizationId,
        List<EmployeeResponse> employees,
        String message
) {}
