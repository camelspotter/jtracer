package net.libcsdbg.jtracer.annotation;

import java.lang.annotation.*;

/* Used to comment on COP design decisions (default mixin, mixin metainfo etc) */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Inherited
public @interface MixinNote
{
	String value();
}
