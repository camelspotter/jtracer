package net.libcsdbg.jtracer.service.log;

import org.qi4j.api.property.Property;

public interface LoggerServiceState
{
	Property<Boolean> active();

	Property<String> metainfo();

	Property<Boolean> mute();
}
