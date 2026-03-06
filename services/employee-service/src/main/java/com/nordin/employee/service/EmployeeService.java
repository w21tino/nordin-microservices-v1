package com.nordin.employee.service;

import com.nordin.employee.dto.EmployeeRequest;
import com.nordin.employee.dto.EmployeeResponse;

import java.util.List;
import java.util.UUID;

/**
 * EmployeeService — Contrato del servicio de negocio.
 *
 * Por qué una interfaz:
 * - Desacopla el contrato de la implementación
 * - Facilita mocking en tests
 * - En V2 permite cambiar la implementación sin tocar
 *   las clases que dependen de este contrato
 */
public interface EmployeeService {

    EmployeeResponse createEmployee(EmployeeRequest request);

    List<EmployeeResponse> getAllEmployees();

    EmployeeResponse getEmployeeById(UUID id);

    List<EmployeeResponse> getEmployeesByDepartmentId(UUID departmentId);
}
