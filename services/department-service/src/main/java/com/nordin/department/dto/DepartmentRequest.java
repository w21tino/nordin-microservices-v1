package com.nordin.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DepartmentRequest — DTO de entrada para crear un departamento.
 */
public record DepartmentRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotNull(message = "El organizationId es obligatorio")
        UUID organizationId
) {}
