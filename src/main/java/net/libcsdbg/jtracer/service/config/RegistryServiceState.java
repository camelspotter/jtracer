package net.libcsdbg.jtracer.service.config;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;

import java.util.Map;

public interface RegistryServiceState
{
	Property<Boolean> active();

	Property<String> metainfo();

	Property<String> source();

	@Optional
	Property<Map<String, String>> state();
}
