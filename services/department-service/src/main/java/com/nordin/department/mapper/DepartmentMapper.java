package com.nordin.department.mapper;

import com.nordin.department.dto.DepartmentRequest;
import com.nordin.department.dto.DepartmentResponse;
import com.nordin.department.dto.EmployeeResponse;
import com.nordin.department.model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * DepartmentMapper — Mapeo Entidad ↔ DTO con MapStruct.
 *
 * toResponse con employees y message: los campos 'employees'
 * y 'message' no existen en la entidad — se pasan explícitamente
 * desde el service después de orquestar la llamada a employee-service.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepartmentMapper {

    Department toEntity(DepartmentRequest request);

    @Mapping(target = "employees", expression = "java(employees)")
    @Mapping(target = "message", expression = "java(message)")
    DepartmentResponse toResponse(Department department,
                                  List<EmployeeResponse> employees,
                                  String message);
}
