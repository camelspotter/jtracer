package net.libcsdbg.jtracer.service.persistence.tools;

import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

public interface FilterState
{
	@Immutable
	Property<Class<? extends Filter>> type();
}
