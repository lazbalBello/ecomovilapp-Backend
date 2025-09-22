package com.EcoTransporte.GestionSeguimiento.Validacion;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class NotEmptyIfNotNullValidator implements ConstraintValidator<NotEmptyIfNotNull,List<?>> {
    @Override
    public boolean isValid(List<?> list, ConstraintValidatorContext context){
        if (list == null)
            return true;
        if (list.isEmpty())
            return false;

        return list.stream().noneMatch(item -> item == null);
    }
}
