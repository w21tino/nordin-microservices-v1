package com.nordin.employee.exception;

import java.util.UUID;

/**
 * EmployeeNotFoundException — Excepción de negocio.
 *
 * Por qué excepciones de negocio propias:
 * - Permiten al @ControllerAdvice distinguir entre
 *   "recurso no encontrado" (404) y "error interno" (500)
 * - El mensaje es claro para el cliente de la API
 * - En Zipkin, el trace mostrará esta excepción con su contexto completo
 */
public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(UUID id) {
        super("Empleado no encontrado con id: " + id);
    }
}
