package com.monjam.core.annotation;

import com.monjam.core.api.MigrationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Migrate {
    MigrationType type();

    String version();

    String description() default "";
}
