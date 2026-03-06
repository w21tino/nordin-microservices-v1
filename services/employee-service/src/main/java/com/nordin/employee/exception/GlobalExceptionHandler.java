package com.nordin.employee.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler — Manejo centralizado de excepciones.
 *
 * Usa ProblemDetail (RFC 7807) — estándar de Spring Boot 3.x
 * para respuestas de error estructuradas. Reemplaza el ResponseEntity
 * manual con un contrato más descriptivo y estandarizado.
 *
 * Formato de respuesta:
 * {
 *   "type": "...",
 *   "title": "...",
 *   "status": 404,
 *   "detail": "Empleado no encontrado con id: ...",
 *   "timestamp": "..."
 * }
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ProblemDetail handleEmployeeNotFound(EmployeeNotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Empleado no encontrado");
        problem.setType(URI.create("/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "Error de validación"
                ));

        log.warn("Error de validación: {}", errors);

        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, "Error en los datos de entrada");
        problem.setTitle("Error de validación");
        problem.setType(URI.create("/errors/validation"));
        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Error interno no esperado: {}", ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        problem.setTitle("Error interno");
        problem.setType(URI.create("/errors/internal"));
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }
}
