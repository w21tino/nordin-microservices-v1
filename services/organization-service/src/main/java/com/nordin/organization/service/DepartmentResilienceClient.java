package com.nordin.organization.service;

import com.nordin.organization.client.DepartmentClient;
import com.nordin.organization.dto.DepartmentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepartmentResilienceClient {

    private static final String DEPARTMENT_SERVICE = "department-service";
    private final DepartmentClient departmentClient;

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


