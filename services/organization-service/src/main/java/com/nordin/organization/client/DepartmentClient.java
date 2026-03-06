package com.nordin.organization.client;

import com.nordin.organization.dto.DepartmentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

/**
 * DepartmentClient — Feign Client para department-service.
 *
 * Llama a GET /api/departments/organization/{organizationId}
 * que ya incluye los empleados anidados de cada departamento.
 *
 * Un solo Feign call retorna el árbol completo:
 * organization → departments → employees
 */
@FeignClient(
        name = "department-service",
        fallback = DepartmentClientFallback.class
)
public interface DepartmentClient {

    @GetMapping("/api/departments/organization/{organizationId}")
    List<DepartmentResponse> getDepartmentsByOrganizationId(@PathVariable UUID organizationId);
}
