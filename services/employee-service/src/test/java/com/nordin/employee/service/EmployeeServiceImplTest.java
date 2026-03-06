package com.nordin.employee.service;

import com.nordin.employee.dto.EmployeeRequest;
import com.nordin.employee.dto.EmployeeResponse;
import com.nordin.employee.exception.EmployeeNotFoundException;
import com.nordin.employee.mapper.EmployeeMapper;
import com.nordin.employee.model.Employee;
import com.nordin.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * EmployeeServiceImplTest — Tests unitarios de la capa de servicio.
 *
 * Decisiones de testing:
 * - @ExtendWith(MockitoExtension): inicializa mocks sin levantar contexto Spring.
 *   Es el test más rápido posible — no hay BD, no hay red.
 * - @Nested + @DisplayName: organiza los tests por método, más legible
 *   y fácil de navegar en el reporte de IntelliJ.
 * - AssertJ sobre JUnit Assertions: API fluida más expresiva.
 *   "assertThat(result).isNotNull().isEqualTo(...)" es más legible
 *   que "assertNotNull(result); assertEquals(..., result)".
 * - Verificamos comportamiento (verify) además de resultado (assertThat):
 *   aseguramos que el service interactúa correctamente con sus dependencias.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService — Tests unitarios")
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private UUID employeeId;
    private UUID departmentId;
    private Employee employee;
    private EmployeeRequest request;
    private EmployeeResponse response;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        departmentId = UUID.randomUUID();

        employee = Employee.builder()
                .id(employeeId)
                .name("Juan Pérez")
                .email("juan@nordin.com")
                .departmentId(departmentId)
                .build();

        request = new EmployeeRequest("Juan Pérez", "juan@nordin.com", departmentId);
        response = new EmployeeResponse(employeeId, "Juan Pérez", "juan@nordin.com", departmentId);
    }

    // ─────────────────────────────────────────────
    // createEmployee
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("createEmployee")
    class CreateEmployee {

        @Test
        @DisplayName("debe crear y retornar el empleado correctamente")
        void shouldCreateEmployeeSuccessfully() {
            when(employeeMapper.toEntity(request)).thenReturn(employee);
            when(employeeRepository.save(employee)).thenReturn(employee);
            when(employeeMapper.toResponse(employee)).thenReturn(response);

            EmployeeResponse result = employeeService.createEmployee(request);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(employeeId);
            assertThat(result.email()).isEqualTo("juan@nordin.com");

            verify(employeeRepository, times(1)).save(any(Employee.class));
        }
    }

    // ─────────────────────────────────────────────
    // getAllEmployees
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("getAllEmployees")
    class GetAllEmployees {

        @Test
        @DisplayName("debe retornar lista de empleados cuando existen")
        void shouldReturnAllEmployees() {
            when(employeeRepository.findAll()).thenReturn(List.of(employee));
            when(employeeMapper.toResponse(employee)).thenReturn(response);

            List<EmployeeResponse> result = employeeService.getAllEmployees();

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Juan Pérez");
        }

        @Test
        @DisplayName("debe retornar lista vacía cuando no hay empleados")
        void shouldReturnEmptyListWhenNoEmployees() {
            when(employeeRepository.findAll()).thenReturn(List.of());

            List<EmployeeResponse> result = employeeService.getAllEmployees();

            assertThat(result).isNotNull().isEmpty();
            verify(employeeMapper, never()).toResponse(any());
        }
    }

    // ─────────────────────────────────────────────
    // getEmployeeById
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("getEmployeeById")
    class GetEmployeeById {

        @Test
        @DisplayName("debe retornar empleado cuando el ID existe")
        void shouldReturnEmployeeWhenFound() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(employeeMapper.toResponse(employee)).thenReturn(response);

            EmployeeResponse result = employeeService.getEmployeeById(employeeId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(employeeId);
        }

        @Test
        @DisplayName("debe lanzar EmployeeNotFoundException cuando el ID no existe")
        void shouldThrowExceptionWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            when(employeeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getEmployeeById(nonExistentId))
                    .isInstanceOf(EmployeeNotFoundException.class)
                    .hasMessageContaining(nonExistentId.toString());

            verify(employeeMapper, never()).toResponse(any());
        }
    }

    // ─────────────────────────────────────────────
    // getEmployeesByDepartmentId
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("getEmployeesByDepartmentId")
    class GetEmployeesByDepartmentId {

        @Test
        @DisplayName("debe retornar empleados del departamento")
        void shouldReturnEmployeesByDepartment() {
            when(employeeRepository.findByDepartmentId(departmentId)).thenReturn(List.of(employee));
            when(employeeMapper.toResponse(employee)).thenReturn(response);

            List<EmployeeResponse> result = employeeService.getEmployeesByDepartmentId(departmentId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).departmentId()).isEqualTo(departmentId);
        }

        @Test
        @DisplayName("debe retornar lista vacía si el departamento no tiene empleados")
        void shouldReturnEmptyWhenNoneFound() {
            when(employeeRepository.findByDepartmentId(departmentId)).thenReturn(List.of());

            List<EmployeeResponse> result = employeeService.getEmployeesByDepartmentId(departmentId);

            assertThat(result).isEmpty();
        }
    }
}
