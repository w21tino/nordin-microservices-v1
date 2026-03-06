package com.nordin.employee.repository;

import com.nordin.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * EmployeeRepository — Acceso a datos con Spring Data JPA.
 *
 * findByDepartmentId: usado por department-service cuando
 * necesita los empleados de un departamento específico.
 * Este método es el punto de entrada para la orquestación
 * desde department-service via Feign.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    List<Employee> findByDepartmentId(UUID departmentId);
}
