package com.ServiciosTransporte.Gestion.Modelos;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.proxy.HibernateProxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Filter(name = "notDeletedFilter", condition = "fecha_eliminacion IS NULL")
@Entity
@Table(name = "Conductor", indexes = {
        @Index(name = "idx_conductor_nombre", columnList = "nombre"),
        @Index(name = "idx_conductor_apellidos", columnList = "apellidos"),
        @Index(name = "idx_conductor_dni", columnList = "dni"),
        @Index(name = "idx_conductor_fecha_eliminacion", columnList = "fecha_eliminacion"),
        @Index(name = "idx_conductor_usuarioId", columnList = "usuario_id")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Conductor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String dni;
    private String nombre;
    private String apellidos;

    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    @ElementCollection
    private List<String> categoriasLicencia;

    private boolean disponibilidad;

    @Column(name = "usuario_id", unique = true)
    private String usuarioId;

    @OneToMany(mappedBy = "conductor" , cascade = CascadeType.ALL , orphanRemoval = true)
    @Filter(name = "notDeletedFilter", condition = "fecha_eliminacion IS NULL")
    @ToString.Exclude
    private List<VehiculoAsignacion> historialAsignaciones;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Conductor conductor = (Conductor) o;
        return getId() != null && Objects.equals(getId(), conductor.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
