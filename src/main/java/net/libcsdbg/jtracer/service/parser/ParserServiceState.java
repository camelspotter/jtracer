package net.libcsdbg.jtracer.service.parser;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;

import java.util.List;
import java.util.Map;

public interface ParserServiceState
{
	Property<Boolean> active();

	Property<String> metainfo();

	@Optional
	Property<Map<String, List<String>>> state();
}
