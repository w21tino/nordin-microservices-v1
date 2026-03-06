package com.nordin.organization.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * OrganizationRequest — DTO de entrada.
 * Organization-service solo tiene GET según las operaciones definidas.
 * Incluimos el request para consistencia y posible extensión futura.
 */
public record OrganizationRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotBlank(message = "La dirección es obligatoria")
        String address
) {}
