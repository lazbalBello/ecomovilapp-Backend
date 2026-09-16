package com.ServiciosTransporte.Gestion.Modelos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.proxy.HibernateProxy;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa la asignación de un conductor a un vehículo.
 *
 * <p>El soft delete está gestionado de forma completamente transparente a través de
 * {@link EntidadBase}: {@code @SQLDelete} convierte los {@code DELETE} de JPA en un
 * {@code UPDATE} que marca {@code fecha_eliminacion}, y {@code @SQLRestriction}
 * (heredado) filtra los registros marcados en todas las consultas JPQL estándar.
 */
@SQLDelete(sql = "UPDATE vehiculo_asignacion SET fecha_eliminacion = NOW() WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"vehiculo_id", "conductor_id"}),
       indexes = {
           @Index(name = "idx_asignacion_fecha_eliminacion", columnList = "fecha_eliminacion"),
           @Index(name = "idx_asignacion_fecha_creacion",    columnList = "fecha_creacion")
       })
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class VehiculoAsignacion extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaInicio;
    private LocalDate fechaFinal;
    private boolean indefinido;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @ManyToOne
    @JoinColumn(name = "conductor_id")
    private Conductor conductor;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        VehiculoAsignacion that = (VehiculoAsignacion) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
