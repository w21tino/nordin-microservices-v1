package com.nordin.employee.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Entidad JPA — Employee.
 *
 * Decisiones de diseño:
 * - UUID como PK: evita colisiones entre servicios y es más seguro
 *   que un Long autoincremental en arquitecturas distribuidas.
 * - departmentId como Long: referencia al departamento SIN FK real.
 *   En microservicios NO usamos JOINs entre BDs — la integridad
 *   referencial se maneja a nivel de aplicación, no de BD.
 * - La entidad NUNCA sale del servicio — se mapea a DTO antes de responder.
 */
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Referencia al departamento.
     * NO es una FK de BD — es solo un identificador lógico.
     * Patrón: cada servicio es dueño de sus datos.
     */
    @Column(nullable = false)
    private UUID departmentId;
}
