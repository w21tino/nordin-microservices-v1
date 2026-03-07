package com.nordin.department.service;

import com.nordin.department.client.EmployeeClient;
import com.nordin.department.dto.DepartmentRequest;
import com.nordin.department.dto.DepartmentResponse;
import com.nordin.department.dto.EmployeeResponse;
import com.nordin.department.exception.DepartmentNotFoundException;
import com.nordin.department.mapper.DepartmentMapper;
import com.nordin.department.model.Department;
import com.nordin.department.repository.DepartmentRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeResilienceClient {

    private final EmployeeClient employeeClient;
    private static final String EMPLOYEE_SERVICE = "employee-service";

    @CircuitBreaker(name = EMPLOYEE_SERVICE, fallbackMethod = "employeesFallback")
    @Retry(name = EMPLOYEE_SERVICE)
    public List<EmployeeResponse> getEmployeesWithResilience(UUID departmentId) {
        log.debug("Llamando a employee-service desde el Wrapper para depto: {}", departmentId);
        return employeeClient.getEmployeesByDepartmentId(departmentId);
    }

    // El fallback DEBE estar aquí junto al método original
    public List<EmployeeResponse> employeesFallback(UUID departmentId, Throwable t) {
        log.warn("Fallback activado para depto {}. Razón: {}", departmentId, t.getMessage());
        return Collections.emptyList();
    }
}
