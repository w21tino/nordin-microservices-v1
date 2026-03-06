package com.nordin.employee.service;

import com.nordin.employee.dto.EmployeeRequest;
import com.nordin.employee.dto.EmployeeResponse;
import com.nordin.employee.exception.EmployeeNotFoundException;
import com.nordin.employee.mapper.EmployeeMapper;
import com.nordin.employee.model.Employee;
import com.nordin.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * EmployeeServiceImpl — Implementación de la lógica de negocio.
 *
 * Decisiones:
 * - @Transactional(readOnly = true) en consultas: optimización
 *   que le indica a la BD que no espere commits — mejora rendimiento.
 * - @Slf4j: logs estructurados en cada operación — esencial para
 *   trazabilidad cuando Zipkin muestra el trace completo.
 * - La capa service nunca retorna entidades — siempre DTOs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("Creando empleado con email: {}", request.email());

        Employee employee = employeeMapper.toEntity(request);
        Employee saved = employeeRepository.save(employee);

        log.info("Empleado creado con id: {}", saved.getId());
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        log.info("Consultando todos los empleados");

        return employeeRepository.findAll()
                .stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(UUID id) {
        log.info("Consultando empleado con id: {}", id);

        return employeeRepository.findById(id)
                .map(employeeMapper::toResponse)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByDepartmentId(UUID departmentId) {
        log.info("Consultando empleados del departamento: {}", departmentId);

        return employeeRepository.findByDepartmentId(departmentId)
                .stream()
                .map(employeeMapper::toResponse)
                .toList();
    }
}
