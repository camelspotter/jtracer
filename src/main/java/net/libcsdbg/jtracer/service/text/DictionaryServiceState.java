package net.libcsdbg.jtracer.service.text;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

import java.util.List;
import java.util.Map;

public interface DictionaryServiceState
{
	Property<Boolean> active();

	@NotEmpty
	Property<String> metainfo();

	@Optional
	Property<Map<String, List<String>>> words();
}
