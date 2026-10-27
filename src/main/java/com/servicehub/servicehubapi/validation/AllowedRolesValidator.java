package com.servicehub.servicehubapi.validation;

import com.servicehub.servicehubapi.model.Role;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.EnumSet;
import java.util.Set;

public class AllowedRolesValidator implements ConstraintValidator<AllowedRoles, Role> {

    private Set<Role> allowed;

    @Override
    public void initialize(AllowedRoles annotation) {
        allowed = EnumSet.noneOf(Role.class);
        allowed.addAll(Set.of(annotation.value()));
    }

    @Override
    public boolean isValid(Role value, ConstraintValidatorContext context) {
        return value == null || allowed.contains(value);
    }
}
