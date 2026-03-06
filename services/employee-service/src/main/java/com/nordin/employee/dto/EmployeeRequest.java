package com.nordin.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * EmployeeRequest — DTO de entrada para crear un empleado.
 *
 * Decisión: separar Request y Response en DTOs distintos.
 * El Request valida entrada, el Response controla qué se expone.
 * Nunca exponemos la entidad directamente al cliente.
 */
public record EmployeeRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene formato válido")
        String email,

        @NotNull(message = "El departmentId es obligatorio")
        UUID departmentId
) {}
