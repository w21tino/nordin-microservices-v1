package com.nordin.department.repository;

import com.nordin.department.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * DepartmentRepository — Acceso a datos.
 *
 * findByOrganizationId: consumido por organization-service
 * via Feign para obtener los departamentos de una organización.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findByOrganizationId(UUID organizationId);
}
