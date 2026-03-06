package com.nordin.organization.dto;

import java.util.List;
import java.util.UUID;

/**
 * OrganizationResponse — DTO de salida con departments anidados.
 *
 * departments: lista obtenida de department-service via Feign.
 *              Cada department ya incluye sus employees.
 *
 * message: indica degradación de department-service.
 *          Si departments está vacío y message tiene valor,
 *          el cliente sabe que es un fallo temporal.
 *
 * Cadena de degradación completa:
 *   employee-service falla → department.message = "⚠️ empleados no disponibles"
 *   department-service falla → organization.departments vacío + organization.message
 */
public record OrganizationResponse(
        UUID id,
        String name,
        String address,
        List<DepartmentResponse> departments,
        String message
) {}
