package net.libcsdbg.jtracer.service.persistence.jar;

import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

public interface JarServiceState
{
	Property<Boolean> active();

	@NotEmpty
	Property<String> metainfo();
}
