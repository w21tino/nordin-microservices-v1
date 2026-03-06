package com.nordin.organization.exception;

import java.util.UUID;

public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException(UUID id) {
        super("Organización no encontrada con id: " + id);
    }
}
