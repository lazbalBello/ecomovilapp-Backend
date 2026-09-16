package com.ServiciosTransporte.Gestion.Modelos;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.proxy.HibernateProxy;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa a un conductor de la flota.
 *
 * <p>El soft delete está gestionado de forma completamente transparente a través de
 * {@link EntidadBase}: {@code @SQLDelete} convierte los {@code DELETE} de JPA en un
 * {@code UPDATE} que marca {@code fecha_eliminacion}, y {@code @SQLRestriction}
 * (heredado) filtra los registros marcados en todas las consultas JPQL estándar.
 */
@SQLDelete(sql = "UPDATE conductor SET fecha_eliminacion = NOW() WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
@Entity
@Table(name = "Conductor", indexes = {
        @Index(name = "idx_conductor_nombre",            columnList = "nombre"),
        @Index(name = "idx_conductor_apellidos",         columnList = "apellidos"),
        @Index(name = "idx_conductor_dni",               columnList = "dni"),
        @Index(name = "idx_conductor_fecha_eliminacion", columnList = "fecha_eliminacion"),
        @Index(name = "idx_conductor_usuarioId",         columnList = "usuario_id"),
        @Index(name = "idx_conductor_fecha_creacion",    columnList = "fecha_creacion")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Conductor extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String dni;
    private String nombre;
    private String apellidos;

    @ElementCollection
    private List<String> categoriasLicencia;

    private boolean disponibilidad;

    @Column(name = "usuario_id", unique = true)
    private String usuarioId;

    @OneToMany(mappedBy = "conductor", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<VehiculoAsignacion> historialAsignaciones;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Conductor conductor = (Conductor) o;
        return getId() != null && Objects.equals(getId(), conductor.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
