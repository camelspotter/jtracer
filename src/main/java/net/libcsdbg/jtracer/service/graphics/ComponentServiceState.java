package net.libcsdbg.jtracer.service.graphics;

import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

public interface ComponentServiceState
{
	Property<Boolean> active();

	@NotEmpty
	Property<String> metainfo();
}
