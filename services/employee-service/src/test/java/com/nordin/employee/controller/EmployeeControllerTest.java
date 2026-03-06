package com.nordin.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nordin.employee.dto.EmployeeRequest;
import com.nordin.employee.dto.EmployeeResponse;
import com.nordin.employee.exception.EmployeeNotFoundException;
import com.nordin.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EmployeeControllerTest — Tests de la capa de presentación con MockMvc.
 *
 * Decisiones:
 * - @WebMvcTest: levanta SOLO el contexto web (controller + filters).
 *   No levanta BD ni beans de servicio — mucho más rápido que @SpringBootTest.
 * - @MockBean: reemplaza el EmployeeService real con un mock de Mockito
 *   inyectado en el contexto Spring.
 * - Validamos: código HTTP, Content-Type y estructura del body JSON.
 *   Eso es exactamente lo que Postman validaría manualmente.
 */
@WebMvcTest(EmployeeController.class)
@DisplayName("EmployeeController — Tests MockMvc")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private UUID employeeId;
    private UUID departmentId;
    private EmployeeResponse response;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        response = new EmployeeResponse(employeeId, "Juan Pérez", "juan@nordin.com", departmentId);
    }

    // ─────────────────────────────────────────────
    // POST /api/employees
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/employees")
    class CreateEmployee {

        @Test
        @DisplayName("debe retornar 201 cuando los datos son válidos")
        void shouldReturn201WhenValidRequest() throws Exception {
            EmployeeRequest request = new EmployeeRequest("Juan Pérez", "juan@nordin.com", departmentId);
            when(employeeService.createEmployee(any())).thenReturn(response);

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(employeeId.toString()))
                    .andExpect(jsonPath("$.name").value("Juan Pérez"))
                    .andExpect(jsonPath("$.email").value("juan@nordin.com"));
        }

        @Test
        @DisplayName("debe retornar 400 cuando el email es inválido")
        void shouldReturn400WhenEmailInvalid() throws Exception {
            EmployeeRequest request = new EmployeeRequest("Juan Pérez", "email-invalido", departmentId);

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("debe retornar 400 cuando el nombre está vacío")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            EmployeeRequest request = new EmployeeRequest("", "juan@nordin.com", departmentId);

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/employees
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/employees")
    class GetAllEmployees {

        @Test
        @DisplayName("debe retornar 200 con lista de empleados")
        void shouldReturn200WithEmployeeList() throws Exception {
            when(employeeService.getAllEmployees()).thenReturn(List.of(response));

            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(employeeId.toString()));
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/employees/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/employees/{id}")
    class GetEmployeeById {

        @Test
        @DisplayName("debe retornar 200 cuando el empleado existe")
        void shouldReturn200WhenFound() throws Exception {
            when(employeeService.getEmployeeById(employeeId)).thenReturn(response);

            mockMvc.perform(get("/api/employees/{id}", employeeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(employeeId.toString()))
                    .andExpect(jsonPath("$.email").value("juan@nordin.com"));
        }

        @Test
        @DisplayName("debe retornar 404 cuando el empleado no existe")
        void shouldReturn404WhenNotFound() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            when(employeeService.getEmployeeById(nonExistentId))
                    .thenThrow(new EmployeeNotFoundException(nonExistentId));

            mockMvc.perform(get("/api/employees/{id}", nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Empleado no encontrado"));
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/employees/department/{departmentId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/employees/department/{departmentId}")
    class GetByDepartment {

        @Test
        @DisplayName("debe retornar 200 con empleados del departamento")
        void shouldReturn200WithDepartmentEmployees() throws Exception {
            when(employeeService.getEmployeesByDepartmentId(departmentId))
                    .thenReturn(List.of(response));

            mockMvc.perform(get("/api/employees/department/{departmentId}", departmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].departmentId").value(departmentId.toString()));
        }
    }
}
