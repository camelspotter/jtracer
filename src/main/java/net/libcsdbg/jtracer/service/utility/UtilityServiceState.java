package net.libcsdbg.jtracer.service.utility;

import org.qi4j.api.property.Property;

public interface UtilityServiceState
{
	Property<Boolean> active();

	Property<String> metainfo();
}
