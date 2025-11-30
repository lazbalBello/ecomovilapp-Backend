package com.ServiciosTransporte.Gestion.Modelos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.proxy.HibernateProxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Filter(name = "notDeletedFilter", condition = "fecha_eliminacion IS NULL")
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"vehiculo_id", "conductor_id"}),
indexes = {@Index(name = "idx_asignacion_fecha_eliminacion", columnList = "fecha_eliminacion")})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class VehiculoAsignacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaInicio;
    private LocalDate fechaFinal;
    private boolean indefinido;

    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id")
    @Filter(name = "notDeletedFilter", condition = "fecha_eliminacion IS NULL")
    private Vehiculo vehiculo;

    @ManyToOne
    @JoinColumn(name = "conductor_id")
    @Filter(name = "notDeletedFilter", condition = "fecha_eliminacion IS NULL")
    private Conductor conductor;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        VehiculoAsignacion that = (VehiculoAsignacion) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
