package net.libcsdbg.jtracer.annotation.constraint;

import org.qi4j.api.constraint.ConstraintDeclaration;

import java.lang.annotation.*;

@ConstraintDeclaration
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Inherited
public @interface RegistryKey
{
}
