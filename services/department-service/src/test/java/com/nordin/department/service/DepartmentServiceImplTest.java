package com.nordin.department.service;

import com.nordin.department.client.EmployeeClient;
import com.nordin.department.dto.DepartmentRequest;
import com.nordin.department.dto.DepartmentResponse;
import com.nordin.department.dto.EmployeeResponse;
import com.nordin.department.exception.DepartmentNotFoundException;
import com.nordin.department.mapper.DepartmentMapper;
import com.nordin.department.model.Department;
import com.nordin.department.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentService — Tests unitarios")
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private EmployeeClient employeeClient;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private UUID departmentId;
    private UUID organizationId;
    private Department department;
    private DepartmentRequest request;
    private DepartmentResponse responseWithEmployees;
    private DepartmentResponse responseWithFallback;
    private EmployeeResponse employee;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        organizationId = UUID.randomUUID();

        department = Department.builder()
                .id(departmentId)
                .name("Ingeniería")
                .organizationId(organizationId)
                .build();

        request = new DepartmentRequest("Ingeniería", organizationId);

        employee = new EmployeeResponse(
                UUID.randomUUID(), "Juan Pérez", "juan@nordin.com", departmentId);

        responseWithEmployees = new DepartmentResponse(
                departmentId, "Ingeniería", organizationId, List.of(employee), null);

        responseWithFallback = new DepartmentResponse(
                departmentId, "Ingeniería", organizationId, Collections.emptyList(),
                "⚠️ Información parcial: servicio de empleados no disponible temporalmente");
    }

    @Nested
    @DisplayName("createDepartment")
    class CreateDepartment {

        @Test
        @DisplayName("debe crear y retornar el departamento correctamente")
        void shouldCreateDepartmentSuccessfully() {
            when(departmentMapper.toEntity(request)).thenReturn(department);
            when(departmentRepository.save(department)).thenReturn(department);
            when(departmentMapper.toResponse(department, Collections.emptyList(), null))
                    .thenReturn(responseWithEmployees);

            DepartmentResponse result = departmentService.createDepartment(request);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(departmentId);
            verify(departmentRepository, times(1)).save(any(Department.class));
        }
    }

    @Nested
    @DisplayName("getDepartmentById")
    class GetDepartmentById {

        @Test
        @DisplayName("debe retornar departamento con empleados cuando employee-service responde")
        void shouldReturnDepartmentWithEmployees() {
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(employeeClient.getEmployeesByDepartmentId(departmentId))
                    .thenReturn(List.of(employee));
            when(departmentMapper.toResponse(department, List.of(employee), null))
                    .thenReturn(responseWithEmployees);

            DepartmentResponse result = departmentService.getDepartmentById(departmentId);

            assertThat(result).isNotNull();
            assertThat(result.message()).isNull();
            assertThat(result.employees()).hasSize(1);
        }

        @Test
        @DisplayName("debe retornar message de degradación cuando employee-service retorna vacío (fallback)")
        void shouldReturnFallbackMessageWhenEmployeeServiceFails() {
            // En tests unitarios @CircuitBreaker no está activo — no hay contexto Spring AOP.
            // Simulamos el resultado del fallback directamente: employeeClient retorna lista vacía,
            // que es exactamente lo que hace EmployeeClientFallback.getEmployeesByDepartmentId().
            // El comportamiento del Circuit Breaker se valida en tests de integración.
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(employeeClient.getEmployeesByDepartmentId(departmentId))
                    .thenReturn(Collections.emptyList());
            when(departmentMapper.toResponse(eq(department), eq(Collections.emptyList()), any()))
                    .thenReturn(responseWithFallback);

            DepartmentResponse result = departmentService.getDepartmentById(departmentId);

            assertThat(result).isNotNull();
            assertThat(result.employees()).isEmpty();
            assertThat(result.message()).contains("⚠️");
        }

        @Test
        @DisplayName("debe lanzar DepartmentNotFoundException cuando el ID no existe")
        void shouldThrowExceptionWhenNotFound() {
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.getDepartmentById(departmentId))
                    .isInstanceOf(DepartmentNotFoundException.class)
                    .hasMessageContaining(departmentId.toString());

            verify(employeeClient, never()).getEmployeesByDepartmentId(any());
        }
    }

    @Nested
    @DisplayName("getDepartmentsByOrganizationId")
    class GetDepartmentsByOrganizationId {

        @Test
        @DisplayName("debe retornar departamentos de la organización")
        void shouldReturnDepartmentsByOrganization() {
            when(departmentRepository.findByOrganizationId(organizationId))
                    .thenReturn(List.of(department));
            when(employeeClient.getEmployeesByDepartmentId(departmentId))
                    .thenReturn(List.of(employee));
            when(departmentMapper.toResponse(department, List.of(employee), null))
                    .thenReturn(responseWithEmployees);

            List<DepartmentResponse> result =
                    departmentService.getDepartmentsByOrganizationId(organizationId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).organizationId()).isEqualTo(organizationId);
        }

        @Test
        @DisplayName("debe retornar lista vacía cuando la organización no tiene departamentos")
        void shouldReturnEmptyWhenNoDepartments() {
            when(departmentRepository.findByOrganizationId(organizationId))
                    .thenReturn(Collections.emptyList());

            List<DepartmentResponse> result =
                    departmentService.getDepartmentsByOrganizationId(organizationId);

            assertThat(result).isEmpty();
            verify(employeeClient, never()).getEmployeesByDepartmentId(any());
        }
    }
}
