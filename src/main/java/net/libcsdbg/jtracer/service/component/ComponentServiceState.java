package net.libcsdbg.jtracer.service.component;

import org.qi4j.api.property.Property;

public interface ComponentServiceState
{
	Property<Boolean> active();

	Property<String> metainfo();
}
