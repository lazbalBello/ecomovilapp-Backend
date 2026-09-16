package com.ServiciosTransporte.Gestion.Modelos;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.proxy.HibernateProxy;
import java.util.Objects;

/**
 * Entidad que representa una parada dentro de una ruta de transporte.
 *
 * <p>El soft delete está gestionado de forma completamente transparente a través de
 * {@link EntidadBase}: {@code @SQLDelete} convierte los {@code DELETE} de JPA en un
 * {@code UPDATE} que marca {@code fecha_eliminacion}, y {@code @SQLRestriction}
 * (heredado) filtra los registros marcados en todas las consultas JPQL estándar.
 */
@SQLDelete(sql = "UPDATE parada SET fecha_eliminacion = NOW() WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
@Entity
@Table(indexes = {
        @Index(name = "idx_parada_nombre",            columnList = "nombre"),
        @Index(name = "idx_parada_fecha_eliminacion", columnList = "fecha_eliminacion"),
        @Index(name = "idx_parada_fecha_creacion",    columnList = "fecha_creacion")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Parada extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private double latitud;
    private double longitud;

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
        Parada parada = (Parada) o;
        return getId() != null && Objects.equals(getId(), parada.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
