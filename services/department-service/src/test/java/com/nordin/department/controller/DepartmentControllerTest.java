package com.nordin.department.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nordin.department.dto.DepartmentRequest;
import com.nordin.department.dto.DepartmentResponse;
import com.nordin.department.dto.EmployeeResponse;
import com.nordin.department.exception.DepartmentNotFoundException;
import com.nordin.department.service.DepartmentService;
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

@WebMvcTest(DepartmentController.class)
@DisplayName("DepartmentController — Tests MockMvc")
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentService departmentService;

    private UUID departmentId;
    private UUID organizationId;
    private DepartmentResponse response;
    private DepartmentResponse responseWithFallback;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        organizationId = UUID.randomUUID();

        EmployeeResponse employee = new EmployeeResponse(
                UUID.randomUUID(), "Juan Pérez", "juan@nordin.com", departmentId);

        response = new DepartmentResponse(
                departmentId, "Ingeniería", organizationId, List.of(employee), null);

        responseWithFallback = new DepartmentResponse(
                departmentId, "Ingeniería", organizationId, Collections.emptyList(),
                "⚠️ Información parcial: servicio de empleados no disponible temporalmente");
    }

    @Nested
    @DisplayName("POST /api/departments")
    class CreateDepartment {

        @Test
        @DisplayName("debe retornar 201 cuando los datos son válidos")
        void shouldReturn201WhenValid() throws Exception {
            DepartmentRequest request = new DepartmentRequest("Ingeniería", organizationId);
            when(departmentService.createDepartment(any())).thenReturn(response);

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(departmentId.toString()))
                    .andExpect(jsonPath("$.name").value("Ingeniería"));
        }

        @Test
        @DisplayName("debe retornar 400 cuando el nombre está vacío")
        void shouldReturn400WhenNameBlank() throws Exception {
            DepartmentRequest request = new DepartmentRequest("", organizationId);

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/departments/{id}")
    class GetDepartmentById {

        @Test
        @DisplayName("debe retornar 200 con empleados cuando todo funciona")
        void shouldReturn200WithEmployees() throws Exception {
            when(departmentService.getDepartmentById(departmentId)).thenReturn(response);

            mockMvc.perform(get("/api/departments/{id}", departmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(departmentId.toString()))
                    .andExpect(jsonPath("$.employees").isArray())
                    .andExpect(jsonPath("$.message").doesNotExist());
        }

        @Test
        @DisplayName("debe retornar 200 con message de degradación cuando employee-service falla")
        void shouldReturn200WithFallbackMessage() throws Exception {
            when(departmentService.getDepartmentById(departmentId)).thenReturn(responseWithFallback);

            mockMvc.perform(get("/api/departments/{id}", departmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.employees").isEmpty())
                    .andExpect(jsonPath("$.message").value(
                            "⚠️ Información parcial: servicio de empleados no disponible temporalmente"));
        }

        @Test
        @DisplayName("debe retornar 404 cuando el departamento no existe")
        void shouldReturn404WhenNotFound() throws Exception {
            when(departmentService.getDepartmentById(departmentId))
                    .thenThrow(new DepartmentNotFoundException(departmentId));

            mockMvc.perform(get("/api/departments/{id}", departmentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Departamento no encontrado"));
        }
    }

    @Nested
    @DisplayName("GET /api/departments/organization/{organizationId}")
    class GetByOrganization {

        @Test
        @DisplayName("debe retornar 200 con departamentos de la organización")
        void shouldReturn200WithDepartments() throws Exception {
            when(departmentService.getDepartmentsByOrganizationId(organizationId))
                    .thenReturn(List.of(response));

            mockMvc.perform(get("/api/departments/organization/{organizationId}", organizationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].organizationId").value(organizationId.toString()));
        }
    }
}
