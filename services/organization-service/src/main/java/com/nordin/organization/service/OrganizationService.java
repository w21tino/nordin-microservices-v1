package com.nordin.organization.service;

import com.nordin.organization.dto.OrganizationRequest;
import com.nordin.organization.dto.OrganizationResponse;

import java.util.List;
import java.util.UUID;

public interface OrganizationService {

    OrganizationResponse createOrganization(OrganizationRequest request);

    List<OrganizationResponse> getAllOrganizations();

    OrganizationResponse getOrganizationById(UUID id);
}
