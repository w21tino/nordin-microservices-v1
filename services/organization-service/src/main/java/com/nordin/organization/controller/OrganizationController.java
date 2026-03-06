package com.nordin.organization.controller;

import com.nordin.organization.dto.OrganizationRequest;
import com.nordin.organization.dto.OrganizationResponse;
import com.nordin.organization.service.OrganizationService;
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
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Organizaciones", description = "Operaciones de gestión de organizaciones")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "Crear una organización")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Organización creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody OrganizationRequest request) {
        log.info("POST /api/organizations");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.createOrganization(request));
    }

    @Operation(summary = "Obtener todas las organizaciones",
            description = "Retorna organizaciones con departments y employees anidados. " +
                    "El campo 'message' indica degradación si algún servicio downstream no está disponible.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        log.info("GET /api/organizations");
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    @Operation(summary = "Obtener organización por ID",
            description = "Retorna la organización completa con departments → employees anidados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organización encontrada"),
            @ApiResponse(responseCode = "404", description = "Organización no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(
            @Parameter(description = "UUID de la organización", required = true)
            @PathVariable UUID id) {
        log.info("GET /api/organizations/{}", id);
        return ResponseEntity.ok(organizationService.getOrganizationById(id));
    }
}
