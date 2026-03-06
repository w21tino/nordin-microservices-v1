package com.nordin.department.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Entidad JPA — Department.
 *
 * organizationId: referencia lógica a la organización.
 * NO es FK real — cada servicio es dueño de sus datos.
 *
 * Los campos 'employees' y 'message' NO son columnas de BD.
 * Son campos transient que se pueblan en el DTO de respuesta
 * cuando se orquesta la llamada a employee-service.
 */
@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private UUID organizationId;
}
