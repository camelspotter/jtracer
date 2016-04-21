package net.libcsdbg.jtracer.service.util;

import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

public interface UtilityServiceState
{
	Property<Boolean> active();

	@NotEmpty
	Property<String> metainfo();
}
