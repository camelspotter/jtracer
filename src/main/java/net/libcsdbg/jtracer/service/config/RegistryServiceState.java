package net.libcsdbg.jtracer.service.config;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;
import org.qi4j.library.constraints.annotation.URI;

import java.util.Map;

public interface RegistryServiceState
{
	Property<Boolean> active();

	@NotEmpty
	Property<String> metainfo();

	@Optional
	Property<Map<String, String>> options();

	@URI
	Property<String> source();
}
