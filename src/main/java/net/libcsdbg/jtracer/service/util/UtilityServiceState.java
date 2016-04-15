package net.libcsdbg.jtracer.service.util;

import org.qi4j.api.property.Property;

public interface UtilityServiceState
{
	Property<Boolean> active();

	Property<String> metainfo();
}
