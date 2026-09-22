package com.ServiciosTransporte.Gestion.Auditoria.Anotaciones;

import com.ServiciosTransporte.Gestion.Auditoria.Modelos.TipoOperacion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar métodos de servicios de negocio cuya ejecución
 * debe ser registrada en la auditoría inmutable de base de datos.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Nombre físico exacto de la tabla en PostgreSQL (ej. "vehiculo", "conductor").
     */
    String tabla();

    /**
     * Tipo de operación efectuada: CREACION, MODIFICACION, ELIMINACION, SOFT_DELETE.
     */
    TipoOperacion operacion();

    /**
     * Clase JPA de la entidad objetivo (ej. Vehiculo.class).
     */
    Class<?> entidad();

    /**
     * Expresión SpEL opcional para extraer el ID de la entidad si no es el primer argumento Long.
     * Ejemplos: "#id", "#dto.id", "#vehiculoId".
     * Si se deja vacía, se auto-detecta por convención (primer argumento Long/Number o del valor retornado).
     */
    String idSpel() default "";
}
