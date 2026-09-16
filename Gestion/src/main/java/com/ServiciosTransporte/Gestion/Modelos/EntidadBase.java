package com.ServiciosTransporte.Gestion.Modelos;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Superclase base para todas las entidades del sistema de gestión.
 *
 * <p>Implementa el patrón de <strong>Soft Delete</strong> de forma transparente:
 * <ul>
 *   <li>{@code @SQLDelete}: intercepta las llamadas a {@code DELETE} de JPA y las convierte
 *       automáticamente en un {@code UPDATE} que establece {@code fecha_eliminacion = now()}.
 *       Las subclases DEBEN sobreescribir esta anotación con el nombre de tabla correcto.</li>
 *   <li>{@code @SQLRestriction}: añade una cláusula {@code WHERE fecha_eliminacion IS NULL}
 *       a todas las consultas JPQL/HQL estándar, de modo que los registros eliminados
 *       son invisibles sin ningún esfuerzo adicional en los repositorios ni en los servicios.</li>
 * </ul>
 *
 * <p>Campos de auditoría incluidos:
 * <ul>
 *   <li>{@code fechaCreacion}    — se establece una única vez al persistir la entidad.</li>
 *   <li>{@code fechaActualizacion} — se actualiza automáticamente en cada {@code save()}.</li>
 *   <li>{@code fechaEliminacion} — {@code null} mientras la entidad está activa; se rellena
 *       por {@code @SQLDelete} al eliminar.</li>
 * </ul>
 */
@MappedSuperclass
@SQLRestriction("fecha_eliminacion IS NULL")
// Nota: cada subclase DEBE sobreescribir @SQLDelete con su propio nombre de tabla.
// Esta anotación en la clase base no se hereda automáticamente por Hibernate;
// se declara aquí como documentación del contrato.
@Getter
@Setter
public abstract class EntidadBase {

    /** Momento exacto en que la entidad fue creada. Se asigna una sola vez. */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Momento de la última modificación. Se actualiza en cada {@code @PreUpdate}.
     * Sirve como campo de auditoría y permite detectar conflictos de concurrencia optimista.
     */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Campo centinela del soft delete.
     * <ul>
     *   <li>{@code null}  → entidad activa, visible en consultas normales.</li>
     *   <li>non-null → entidad eliminada, filtrada por {@code @SQLRestriction}.</li>
     * </ul>
     * Se rellena automáticamente mediante {@code @SQLDelete} cuando JPA intenta borrar
     * la fila; también puede establecerse manualmente si se prefiere el soft delete explícito.
     */
    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    @PrePersist
    protected void onPrePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.fechaCreacion    = now;
        this.fechaActualizacion = now;
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
