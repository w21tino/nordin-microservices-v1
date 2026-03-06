package com.nordin.department.service;

import com.nordin.department.dto.DepartmentRequest;
import com.nordin.department.dto.DepartmentResponse;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {

    DepartmentResponse createDepartment(DepartmentRequest request);

    List<DepartmentResponse> getAllDepartments();

    DepartmentResponse getDepartmentById(UUID id);

    List<DepartmentResponse> getDepartmentsByOrganizationId(UUID organizationId);
}
