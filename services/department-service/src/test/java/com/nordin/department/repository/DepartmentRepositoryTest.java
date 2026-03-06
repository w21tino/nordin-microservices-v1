package com.nordin.department.repository;

import com.nordin.department.model.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DepartmentRepositoryTest — Tests con H2 en memoria.
 *
 * Misma decisión que employee-service:
 * H2 en desarrollo local, Testcontainers + PostgreSQL real en CI/CD.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:dept_test_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@DisplayName("DepartmentRepository — Tests con H2 en memoria")
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    private UUID organizationId;
    private Department department;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAll();
        organizationId = UUID.randomUUID();

        department = Department.builder()
                .name("Ingeniería")
                .organizationId(organizationId)
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("debe persistir y generar UUID automáticamente")
        void shouldSaveAndGenerateUUID() {
            Department saved = departmentRepository.save(department);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("Ingeniería");
            assertThat(saved.getOrganizationId()).isEqualTo(organizationId);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("debe encontrar departamento por ID existente")
        void shouldFindById() {
            Department saved = departmentRepository.save(department);

            Optional<Department> result = departmentRepository.findById(saved.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Ingeniería");
        }

        @Test
        @DisplayName("debe retornar vacío cuando el ID no existe")
        void shouldReturnEmptyWhenNotFound() {
            Optional<Department> result = departmentRepository.findById(UUID.randomUUID());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOrganizationId")
    class FindByOrganizationId {

        @Test
        @DisplayName("debe retornar departamentos de la organización")
        void shouldReturnDepartmentsByOrganization() {
            departmentRepository.save(department);

            Department otro = Department.builder()
                    .name("Marketing")
                    .organizationId(organizationId)
                    .build();
            departmentRepository.save(otro);

            List<Department> result = departmentRepository.findByOrganizationId(organizationId);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Department::getName)
                    .containsExactlyInAnyOrder("Ingeniería", "Marketing");
        }

        @Test
        @DisplayName("debe retornar vacío cuando la organización no tiene departamentos")
        void shouldReturnEmptyWhenNoDepartments() {
            List<Department> result = departmentRepository.findByOrganizationId(UUID.randomUUID());

            assertThat(result).isEmpty();
        }
    }
}
