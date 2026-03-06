package com.nordin.organization.client;

import com.nordin.organization.dto.DepartmentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * DepartmentClientFallback — Graceful degradation cuando
 * department-service no está disponible.
 *
 * Retorna lista vacía. OrganizationService detecta esto
 * y puebla el campo 'message' con el aviso de degradación.
 */
@Component
@Slf4j
public class DepartmentClientFallback implements DepartmentClient {

    @Override
    public List<DepartmentResponse> getDepartmentsByOrganizationId(UUID organizationId) {
        log.warn("⚠️ Fallback activado para department-service — organizationId: {}", organizationId);
        return Collections.emptyList();
    }
}
