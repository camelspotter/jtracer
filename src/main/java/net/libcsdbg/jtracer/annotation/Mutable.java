package net.libcsdbg.jtracer.annotation;

import java.lang.annotation.*;

/**
 * Used to describe type mutability. Mutability in this context means that fields can be altered, out of the object control.
 * For example if the object has a getter for a reference of a non-final, mutable field, it is considered mutable. Again, if
 * the object has a setter that just copies the argument directly onto its field, without any control logic, it is considered
 * mutable
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Inherited
public @interface Mutable
{
	String note() default "undefined";

	boolean value() default true;
}
