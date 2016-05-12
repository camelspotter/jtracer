package net.libcsdbg.jtracer.annotation;

import java.lang.annotation.*;

/* Used mainly to designate composite factories, but it is suitable for generic factories as well */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Factory
{
	Type value();

	public static enum Type
	{
		ACTOR,

		ASPECT,

		COMPOSITE,

		COMPOSITE_ENTITY,

		COMPOSITE_VALUE,

		POJO
	}
}
