package com.nordin.organization.repository;

import com.nordin.organization.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:org_test_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@DisplayName("OrganizationRepository — Tests con H2 en memoria")
class OrganizationRepositoryTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization organization;

    @BeforeEach
    void setUp() {
        organizationRepository.deleteAll();

        organization = Organization.builder()
                .name("Nordin Corp")
                .address("Calle Principal 123")
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("debe persistir y generar UUID automáticamente")
        void shouldSaveAndGenerateUUID() {
            Organization saved = organizationRepository.save(organization);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("Nordin Corp");
            assertThat(saved.getAddress()).isEqualTo("Calle Principal 123");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("debe encontrar organización por ID existente")
        void shouldFindById() {
            Organization saved = organizationRepository.save(organization);

            Optional<Organization> result = organizationRepository.findById(saved.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Nordin Corp");
        }

        @Test
        @DisplayName("debe retornar vacío cuando el ID no existe")
        void shouldReturnEmptyWhenNotFound() {
            Optional<Organization> result = organizationRepository.findById(
                    java.util.UUID.randomUUID());

            assertThat(result).isEmpty();
        }
    }
}
