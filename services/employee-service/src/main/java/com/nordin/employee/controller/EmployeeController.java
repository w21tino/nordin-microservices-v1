package com.nordin.employee.controller;

import com.nordin.employee.dto.EmployeeRequest;
import com.nordin.employee.dto.EmployeeResponse;
import com.nordin.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * EmployeeController — Capa de presentación REST.
 * @Tag agrupa todos los endpoints bajo "Empleados" en Swagger UI.
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Empleados", description = "Operaciones de gestión de empleados")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "Crear un empleado",
            description = "Registra un nuevo empleado. Requiere nombre, email único y departmentId válido.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empleado creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        log.info("POST /api/employees");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(request));
    }

    @Operation(summary = "Obtener todos los empleados",
            description = "Retorna la lista completa de empleados registrados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        log.info("GET /api/employees");
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Operation(summary = "Obtener empleado por ID",
            description = "Busca y retorna un empleado específico por su UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empleado encontrado"),
            @ApiResponse(responseCode = "404", description = "Empleado no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @Parameter(description = "UUID del empleado", required = true)
            @PathVariable UUID id) {
        log.info("GET /api/employees/{}", id);
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Operation(summary = "Obtener empleados por departamento",
            description = "Retorna empleados de un departamento. Consumido por department-service via Feign.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empleados obtenidos exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByDepartmentId(
            @Parameter(description = "UUID del departamento", required = true)
            @PathVariable UUID departmentId) {
        log.info("GET /api/employees/department/{}", departmentId);
        return ResponseEntity.ok(employeeService.getEmployeesByDepartmentId(departmentId));
    }
}
