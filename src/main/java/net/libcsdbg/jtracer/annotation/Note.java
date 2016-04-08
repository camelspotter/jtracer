package net.libcsdbg.jtracer.annotation;

import java.lang.annotation.*;

/* Used to comment on generic, important design decisions (high value comments) */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.ANNOTATION_TYPE,
          ElementType.CONSTRUCTOR,
          ElementType.FIELD,
          ElementType.LOCAL_VARIABLE,
          ElementType.METHOD,
          ElementType.PACKAGE,
          ElementType.PARAMETER,
          ElementType.TYPE,
          ElementType.TYPE_PARAMETER,
          ElementType.TYPE_USE })
@Inherited
public @interface Note
{
	String value();
}
