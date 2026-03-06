package com.nordin.employee.repository;

import com.nordin.employee.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EmployeeRepositoryTest — Tests de la capa de acceso a datos.
 *
 * Decisión de ambiente:
 * Usamos H2 en memoria para este test por un conflicto de versiones
 * entre docker-java (usado por Testcontainers) y el Docker Engine
 * del ambiente de desarrollo local.
 *
 * En CI/CD (GitHub Actions) este test se reemplaza por la versión
 * con Testcontainers + PostgreSQL real, garantizando validación
 * contra el motor correcto antes de cualquier merge a main.
 *
 * @DataJpaTest levanta SOLO el contexto JPA — sin controllers,
 * sin services, sin configuración web. Es el test más liviano
 * posible para validar queries y comportamiento del repository.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("EmployeeRepository — Tests con H2 en memoria")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    private UUID departmentId;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentId = UUID.randomUUID();

        employee = Employee.builder()
                .name("Juan Pérez")
                .email("juan@nordin.com")
                .departmentId(departmentId)
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("debe persistir y generar UUID automáticamente")
        void shouldSaveAndGenerateUUID() {
            Employee saved = employeeRepository.save(employee);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("Juan Pérez");
            assertThat(saved.getEmail()).isEqualTo("juan@nordin.com");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("debe encontrar empleado por ID existente")
        void shouldFindById() {
            Employee saved = employeeRepository.save(employee);

            Optional<Employee> result = employeeRepository.findById(saved.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("juan@nordin.com");
        }

        @Test
        @DisplayName("debe retornar vacío cuando el ID no existe")
        void shouldReturnEmptyWhenNotFound() {
            Optional<Employee> result = employeeRepository.findById(UUID.randomUUID());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByDepartmentId")
    class FindByDepartmentId {

        @Test
        @DisplayName("debe retornar empleados del departamento")
        void shouldReturnEmployeesByDepartment() {
            employeeRepository.save(employee);

            Employee otherEmployee = Employee.builder()
                    .name("María López")
                    .email("maria@nordin.com")
                    .departmentId(departmentId)
                    .build();
            employeeRepository.save(otherEmployee);

            List<Employee> result = employeeRepository.findByDepartmentId(departmentId);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Employee::getEmail)
                    .containsExactlyInAnyOrder("juan@nordin.com", "maria@nordin.com");
        }

        @Test
        @DisplayName("debe retornar vacío cuando el departamento no tiene empleados")
        void shouldReturnEmptyWhenDepartmentHasNoEmployees() {
            List<Employee> result = employeeRepository.findByDepartmentId(UUID.randomUUID());

            assertThat(result).isEmpty();
        }
    }
}
