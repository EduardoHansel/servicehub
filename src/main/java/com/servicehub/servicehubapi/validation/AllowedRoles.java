package com.servicehub.servicehubapi.validation;

import com.servicehub.servicehubapi.model.Role;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restringe quais perfis podem ser informados pelo cliente (evita Mass Assignment / escalada
 * de privilégio, ex.: auto-cadastro como ADMIN). Valores nulos são considerados válidos.
 */
@Documented
@Constraint(validatedBy = AllowedRolesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedRoles {

    Role[] value();

    String message() default "Perfil não permitido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
