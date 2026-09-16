package com.ServiciosTransporte.Gestion.Modelos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.proxy.HibernateProxy;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa una ruta del sistema de transporte.
 *
 * <p>El soft delete está gestionado de forma completamente transparente a través de
 * {@link EntidadBase}: {@code @SQLDelete} convierte los {@code DELETE} de JPA en un
 * {@code UPDATE} que marca {@code fecha_eliminacion}, y {@code @SQLRestriction}
 * (heredado) filtra los registros marcados en todas las consultas JPQL estándar.
 */
@SQLDelete(sql = "UPDATE ruta SET fecha_eliminacion = NOW() WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
@Entity
@Table(indexes = {
        @Index(name = "idx_ruta_nombre",            columnList = "nombre"),
        @Index(name = "idx_ruta_fecha_eliminacion", columnList = "fecha_eliminacion"),
        @Index(name = "idx_ruta_fecha_creacion",    columnList = "fecha_creacion")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Ruta extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ElementCollection
    @CollectionTable(name = "coordenada_ruta", joinColumns = @JoinColumn(name = "ruta_id"))
    @OrderBy("orden ASC")
    @JsonIgnore
    private List<RecorridoRuta> recorrido;

    private String descripcion;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Parada> paradas;

    @OneToMany(mappedBy = "ruta")
    @ToString.Exclude
    private List<Vehiculo> vehiculosAsignados;

    @PrePersist
    @PreUpdate
    private void ordenarCoordenadas() {
        if (recorrido != null) {
            for (int i = 0; i < recorrido.size(); i++)
                recorrido.get(i).setOrden(i + 1);
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Ruta ruta = (Ruta) o;
        return getId() != null && Objects.equals(getId(), ruta.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
