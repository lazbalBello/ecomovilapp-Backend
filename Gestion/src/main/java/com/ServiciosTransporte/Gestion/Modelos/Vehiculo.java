package com.ServiciosTransporte.Gestion.Modelos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.proxy.HibernateProxy;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa un vehículo de la flota.
 *
 * <p>El soft delete está gestionado de forma completamente transparente a través de
 * {@link EntidadBase}:
 * <ul>
 *   <li>{@code @SQLDelete} convierte las llamadas {@code DELETE} de JPA en un
 *       {@code UPDATE vehiculo SET fecha_eliminacion = now() WHERE id = ?}</li>
 *   <li>{@code @SQLRestriction} (heredado de {@link EntidadBase}) añade automáticamente
 *       {@code WHERE fecha_eliminacion IS NULL} a todas las consultas JPQL/HQL estándar.</li>
 * </ul>
 */
@SQLDelete(sql = "UPDATE vehiculo SET fecha_eliminacion = NOW() WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
@Entity
@Table(indexes = {
        @Index(name = "idx_vehiculo_matricula",        columnList = "matricula"),
        @Index(name = "idx_vehiculo_gps_id",           columnList = "gps_id"),
        @Index(name = "idx_vehiculo_fecha_eliminacion", columnList = "fecha_eliminacion"),
        @Index(name = "idx_vehiculo_fecha_creacion",   columnList = "fecha_creacion")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Vehiculo extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String matricula;

    @Column(name = "gps_id", unique = true)
    private String gpsId;

    private int capacidadPersonas;
    private String modelo;
    private String marca;
    private String tipoBateria;

    @Enumerated(EnumType.STRING)
    private EstadoVehiculo estado;

    @OneToMany(mappedBy = "vehiculo", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<VehiculoAsignacion> asignaciones;

    @ManyToOne
    @JoinColumn(name = "ruta_id")
    private Ruta ruta;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Vehiculo vehiculo = (Vehiculo) o;
        return getId() != null && Objects.equals(getId(), vehiculo.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
