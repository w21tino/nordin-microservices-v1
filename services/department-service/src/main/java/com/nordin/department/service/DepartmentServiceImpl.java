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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * DepartmentServiceImpl — Lógica de negocio con resiliencia.
 *
 * Patrón de resiliencia aplicado en getEmployeesForDepartment:
 *
 * 1. @Retry: intenta la llamada hasta 3 veces antes de abrir el circuito.
 *    Útil para fallos transitorios de red.
 *
 * 2. @CircuitBreaker: después de N fallos consecutivos, abre el circuito
 *    y llama directamente al fallback sin intentar la llamada real.
 *    Protege a employee-service de sobrecarga cuando está degradado.
 *
 * 3. Fallback: retorna lista vacía + message descriptivo.
 *    El cliente recibe información parcial pero no un error 500.
 *
 * Esto demuestra Graceful Degradation — el sistema falla de forma
 * controlada y comunica el estado al cliente.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private static final String EMPLOYEE_SERVICE = "employee-service";
    private static final String FALLBACK_MESSAGE =
            "⚠️ Información parcial: servicio de empleados no disponible temporalmente";

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final EmployeeClient employeeClient;

    private final EmployeeResilienceClient employeeResilienceClient;


    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        log.info("Creando departamento: {}", request.name());

        Department department = departmentMapper.toEntity(request);
        Department saved = departmentRepository.save(department);

        log.info("Departamento creado con id: {}", saved.getId());
        return departmentMapper.toResponse(saved, Collections.emptyList(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        log.info("Consultando todos los departamentos");

        return departmentRepository.findAll()
                .stream()
                .map(dept -> {
                    List<EmployeeResponse> employees = employeeResilienceClient.getEmployeesWithResilience(dept.getId());
                    String message = employees.isEmpty() ? FALLBACK_MESSAGE : null;
                    return departmentMapper.toResponse(dept, employees, message);
                })
                .toList();
    }



    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(UUID id) {
        log.info("Consultando departamento con id: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));

        // AHORA SÍ: Esta llamada pasa por el Proxy de Spring
        List<EmployeeResponse> employees = employeeResilienceClient.getEmployeesWithResilience(id);

        String message = employees.isEmpty() ? FALLBACK_MESSAGE : null;
        return departmentMapper.toResponse(department, employees, message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartmentsByOrganizationId(UUID organizationId) {
        log.info("Consultando departamentos de organización: {}", organizationId);

        return departmentRepository.findByOrganizationId(organizationId)
                .stream()
                .map(dept -> {
                    List<EmployeeResponse> employees = getEmployeesWithResilience(dept.getId());
                    String message = employees.isEmpty() ? FALLBACK_MESSAGE : null;
                    return departmentMapper.toResponse(dept, employees, message);
                })
                .toList();
    }

    /**
     * Llamada a employee-service con Circuit Breaker + Retry.
     *
     * @CircuitBreaker: si falla, llama a employeesFallback.
     * @Retry: reintenta hasta 3 veces antes de activar el Circuit Breaker.
     *
     * El nombre "employee-service" debe coincidir con la configuración
     * en application.yml bajo resilience4j.circuitbreaker.instances.
     */
    @CircuitBreaker(name = EMPLOYEE_SERVICE, fallbackMethod = "employeesFallback")
    @Retry(name = EMPLOYEE_SERVICE)
    public List<EmployeeResponse> getEmployeesWithResilience(UUID departmentId) {
        log.debug("Llamando a employee-service para departamento: {}", departmentId);
        return employeeClient.getEmployeesByDepartmentId(departmentId);
    }



    /**
     * Fallback del Circuit Breaker.
     * Se activa cuando employee-service no está disponible.
     * Retorna lista vacía — el service pueblo el mensaje de degradación.
     */
    public List<EmployeeResponse> employeesFallback(UUID departmentId, Exception ex) {
        log.warn("⚠️ Circuit Breaker activado para departamento: {} — Causa: {}",
                departmentId, ex.getMessage());
        return Collections.emptyList();
    }
}
