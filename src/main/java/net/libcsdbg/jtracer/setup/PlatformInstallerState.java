package net.libcsdbg.jtracer.setup;

import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

import static net.libcsdbg.jtracer.service.util.PlatformDetectionApi.Platform;

public interface PlatformInstallerState
{
	@Immutable
	Property<Platform> platform();
}
