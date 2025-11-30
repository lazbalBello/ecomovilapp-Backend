package com.ServiciosTransporte.Gestion.Validacion;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotEmptyIfNotNullValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyIfNotNull {
    String message() default "Si se quiere editar el recorrido no debe estar vacío";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
