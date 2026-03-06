package com.nordin.organization.service;

import com.nordin.organization.client.DepartmentClient;
import com.nordin.organization.dto.DepartmentResponse;
import com.nordin.organization.dto.OrganizationRequest;
import com.nordin.organization.dto.OrganizationResponse;
import com.nordin.organization.exception.OrganizationNotFoundException;
import com.nordin.organization.mapper.OrganizationMapper;
import com.nordin.organization.model.Organization;
import com.nordin.organization.repository.OrganizationRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * OrganizationServiceImpl — Orquestador principal del dominio.
 *
 * Flujo completo de una petición GET /api/organizations/{id}:
 *
 * 1. Busca la organización en org-db
 * 2. Llama a department-service via Feign con Circuit Breaker
 * 3. department-service llama internamente a employee-service
 * 4. Retorna el árbol completo: org → departments → employees
 *
 * Si department-service falla:
 * - Circuit Breaker activa departmentsFallback
 * - departments = [] y message = "⚠️ departamentos no disponibles"
 * - La organización se retorna igual — degradación parcial
 *
 * Si solo employee-service falla (department-service responde):
 * - departments se retorna con employees = []
 * - Cada DepartmentResponse tiene su propio message de degradación
 * - organization.message = null (department-service respondió bien)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {

    private static final String DEPARTMENT_SERVICE = "department-service";
    private static final String FALLBACK_MESSAGE =
            "⚠️ Información parcial: servicio de departamentos no disponible temporalmente";

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final DepartmentClient departmentClient;

    @Override
    @Transactional
    public OrganizationResponse createOrganization(OrganizationRequest request) {
        log.info("Creando organización: {}", request.name());

        Organization organization = organizationMapper.toEntity(request);
        Organization saved = organizationRepository.save(organization);

        log.info("Organización creada con id: {}", saved.getId());
        return organizationMapper.toResponse(saved, Collections.emptyList(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAllOrganizations() {
        log.info("Consultando todas las organizaciones");

        return organizationRepository.findAll()
                .stream()
                .map(org -> {
                    List<DepartmentResponse> departments = getDepartmentsWithResilience(org.getId());
                    String message = departments.isEmpty() ? FALLBACK_MESSAGE : null;
                    return organizationMapper.toResponse(org, departments, message);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(UUID id) {
        log.info("Consultando organización con id: {}", id);

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException(id));

        List<DepartmentResponse> departments = getDepartmentsWithResilience(id);
        String message = departments.isEmpty() ? FALLBACK_MESSAGE : null;

        return organizationMapper.toResponse(organization, departments, message);
    }

    @CircuitBreaker(name = DEPARTMENT_SERVICE, fallbackMethod = "departmentsFallback")
    @Retry(name = DEPARTMENT_SERVICE)
    public List<DepartmentResponse> getDepartmentsWithResilience(UUID organizationId) {
        log.debug("Llamando a department-service para organización: {}", organizationId);
        return departmentClient.getDepartmentsByOrganizationId(organizationId);
    }

    public List<DepartmentResponse> departmentsFallback(UUID organizationId, Exception ex) {
        log.warn("⚠️ Circuit Breaker activado para organización: {} — Causa: {}",
                organizationId, ex.getMessage());
        return Collections.emptyList();
    }
}
