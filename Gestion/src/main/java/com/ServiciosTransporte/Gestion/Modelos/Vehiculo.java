package com.ServiciosTransporte.Gestion.Modelos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.proxy.HibernateProxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@FilterDef(name = "notDeletedFilter")
@Filter(name = "notDeletedFilter", condition = "fecha_eliminacion IS NULL")
@Entity
@Table(indexes = {
        @Index(name = "idx_vehiculo_matricula", columnList = "matricula"),
        @Index(name = "idx_vehiculo_fecha_eliminacion", columnList = "fecha_eliminacion")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String matricula;
    private int capacidadPersonas;
    private String modelo;
    private String marca;
    private String tipoBateria;

    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    @Enumerated(EnumType.STRING)
    private EstadoVehiculo estado;


    @OneToMany(mappedBy = "vehiculo" , cascade = CascadeType.ALL , orphanRemoval = true)
    @Filter(name = "notDeletedFilter", condition = "fecha_eliminacion IS NULL")
    @ToString.Exclude
    private List<VehiculoAsignacion> asignaciones;


    @ManyToOne
    @JoinColumn(name = "ruta_id")
    private Ruta ruta;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Vehiculo vehiculo = (Vehiculo) o;
        return getId() != null && Objects.equals(getId(), vehiculo.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
