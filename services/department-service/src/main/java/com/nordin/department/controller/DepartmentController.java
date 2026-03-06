package com.nordin.department.controller;

import com.nordin.department.dto.DepartmentRequest;
import com.nordin.department.dto.DepartmentResponse;
import com.nordin.department.service.DepartmentService;
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

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Departamentos", description = "Operaciones de gestión de departamentos")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "Crear un departamento",
            description = "Registra un nuevo departamento. El campo 'message' en la respuesta " +
                    "indica degradación si employee-service no está disponible.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Departamento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {
        log.info("POST /api/departments");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentService.createDepartment(request));
    }

    @Operation(summary = "Obtener todos los departamentos",
            description = "Retorna departamentos con sus empleados. Si employee-service no está " +
                    "disponible, retorna empleados vacíos con mensaje de degradación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        log.info("GET /api/departments");
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @Operation(summary = "Obtener departamento por ID",
            description = "Busca un departamento con sus empleados anidados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Departamento no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(
            @Parameter(description = "UUID del departamento", required = true)
            @PathVariable UUID id) {
        log.info("GET /api/departments/{}", id);
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @Operation(summary = "Obtener departamentos por organización",
            description = "Retorna todos los departamentos de una organización. " +
                    "Consumido internamente por organization-service via Feign.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departamentos obtenidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<DepartmentResponse>> getDepartmentsByOrganizationId(
            @Parameter(description = "UUID de la organización", required = true)
            @PathVariable UUID organizationId) {
        log.info("GET /api/departments/organization/{}", organizationId);
        return ResponseEntity.ok(departmentService.getDepartmentsByOrganizationId(organizationId));
    }
}
