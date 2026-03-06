package com.nordin.department.exception;

import java.util.UUID;

public class DepartmentNotFoundException extends RuntimeException {

    public DepartmentNotFoundException(UUID id) {
        super("Departamento no encontrado con id: " + id);
    }
}
