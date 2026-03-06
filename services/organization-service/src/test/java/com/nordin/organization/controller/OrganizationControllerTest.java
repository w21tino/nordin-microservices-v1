package com.nordin.organization.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nordin.organization.dto.DepartmentResponse;
import com.nordin.organization.dto.EmployeeResponse;
import com.nordin.organization.dto.OrganizationRequest;
import com.nordin.organization.dto.OrganizationResponse;
import com.nordin.organization.exception.OrganizationNotFoundException;
import com.nordin.organization.service.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizationController.class)
@DisplayName("OrganizationController — Tests MockMvc")
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationService organizationService;

    private UUID organizationId;
    private OrganizationResponse response;
    private OrganizationResponse responseWithFallback;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        EmployeeResponse employee = new EmployeeResponse(
                UUID.randomUUID(), "Juan Pérez", "juan@nordin.com", departmentId);

        DepartmentResponse department = new DepartmentResponse(
                departmentId, "Ingeniería", organizationId, List.of(employee), null);

        response = new OrganizationResponse(
                organizationId, "Nordin Corp", "Calle Principal 123",
                List.of(department), null);

        responseWithFallback = new OrganizationResponse(
                organizationId, "Nordin Corp", "Calle Principal 123",
                Collections.emptyList(),
                "⚠️ Información parcial: servicio de departamentos no disponible temporalmente");
    }

    @Nested
    @DisplayName("POST /api/organizations")
    class CreateOrganization {

        @Test
        @DisplayName("debe retornar 201 cuando los datos son válidos")
        void shouldReturn201WhenValid() throws Exception {
            OrganizationRequest request = new OrganizationRequest("Nordin Corp", "Calle Principal 123");
            when(organizationService.createOrganization(any())).thenReturn(response);

            mockMvc.perform(post("/api/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(organizationId.toString()))
                    .andExpect(jsonPath("$.name").value("Nordin Corp"));
        }

        @Test
        @DisplayName("debe retornar 400 cuando el nombre está vacío")
        void shouldReturn400WhenNameBlank() throws Exception {
            OrganizationRequest request = new OrganizationRequest("", "Calle Principal 123");

            mockMvc.perform(post("/api/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/organizations/{id}")
    class GetOrganizationById {

        @Test
        @DisplayName("debe retornar 200 con árbol completo org→dept→emp")
        void shouldReturn200WithFullTree() throws Exception {
            when(organizationService.getOrganizationById(organizationId)).thenReturn(response);

            mockMvc.perform(get("/api/organizations/{id}", organizationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(organizationId.toString()))
                    .andExpect(jsonPath("$.departments").isArray())
                    .andExpect(jsonPath("$.departments[0].employees").isArray())
                    .andExpect(jsonPath("$.message").doesNotExist());
        }

        @Test
        @DisplayName("debe retornar 200 con message cuando department-service falla")
        void shouldReturn200WithFallback() throws Exception {
            when(organizationService.getOrganizationById(organizationId))
                    .thenReturn(responseWithFallback);

            mockMvc.perform(get("/api/organizations/{id}", organizationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.departments").isEmpty())
                    .andExpect(jsonPath("$.message").value(
                            "⚠️ Información parcial: servicio de departamentos no disponible temporalmente"));
        }

        @Test
        @DisplayName("debe retornar 404 cuando la organización no existe")
        void shouldReturn404WhenNotFound() throws Exception {
            when(organizationService.getOrganizationById(organizationId))
                    .thenThrow(new OrganizationNotFoundException(organizationId));

            mockMvc.perform(get("/api/organizations/{id}", organizationId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Organización no encontrada"));
        }
    }

    @Nested
    @DisplayName("GET /api/organizations")
    class GetAllOrganizations {

        @Test
        @DisplayName("debe retornar 200 con lista de organizaciones")
        void shouldReturn200WithList() throws Exception {
            when(organizationService.getAllOrganizations()).thenReturn(List.of(response));

            mockMvc.perform(get("/api/organizations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("Nordin Corp"));
        }
    }
}
