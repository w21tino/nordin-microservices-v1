package com.nordin.employee.mapper;

import com.nordin.employee.dto.EmployeeRequest;
import com.nordin.employee.dto.EmployeeResponse;
import com.nordin.employee.model.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * EmployeeMapper — Mapeo Entidad ↔ DTO con MapStruct.
 *
 * Por qué MapStruct sobre ModelMapper:
 * - Genera código en tiempo de compilación (compile-time)
 * - Si un campo cambia y el mapeo queda inconsistente,
 *   el build falla — no el runtime. Eso es seguridad real.
 * - Sin reflection en runtime → mejor rendimiento
 *
 * componentModel = SPRING: MapStruct genera un @Component
 * que Spring puede inyectar normalmente con @Autowired.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeMapper {

    Employee toEntity(EmployeeRequest request);

    EmployeeResponse toResponse(Employee employee);
}
