package com.ServiciosTransporte.Gestion.Auditoria.Modelos;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Registro inmutable de auditoría para operaciones sobre entidades del sistema.
 */
@Entity
@Table(name = "auditoria_log", indexes = {
        @Index(name = "idx_auditoria_tabla_entidad", columnList = "nombre_tabla, entidad_id"),
        @Index(name = "idx_auditoria_fecha", columnList = "fecha_operacion"),
        @Index(name = "idx_auditoria_tipo", columnList = "tipo_operacion"),
        @Index(name = "idx_auditoria_usuario", columnList = "usuario_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_tabla", nullable = false, length = 100)
    private String nombreTabla;

    @Column(name = "entidad_id", nullable = false)
    private Long entidadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false, length = 30)
    private TipoOperacion tipoOperacion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_anteriores", columnDefinition = "jsonb")
    private JsonNode datosAnteriores;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_nuevos", columnDefinition = "jsonb")
    private JsonNode datosNuevos;

    @Column(name = "usuario_id", nullable = false)
    private String usuarioId;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDateTime fechaOperacion;
}
