package com.nordin.employee.dto;

import java.util.UUID;

/**
 * EmployeeResponse — DTO de salida.
 *
 * Usando Java Record para DTOs de respuesta:
 * - Inmutable por naturaleza
 * - Menos boilerplate que una clase con Lombok
 * - Serializa correctamente con Jackson
 *
 * Expone exactamente lo que el cliente necesita ver,
 * sin filtrar campos internos de la entidad.
 */
public record EmployeeResponse(
        UUID id,
        String name,
        String email,
        UUID departmentId
) {}
