package net.libcsdbg.jtracer.annotation.constraint;

import org.qi4j.api.constraint.ConstraintDeclaration;

import java.lang.annotation.*;

@ConstraintDeclaration
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Inherited
public @interface Name
{
	String grammar() default "^[a-zA-Z0-9_-]+$";
}
