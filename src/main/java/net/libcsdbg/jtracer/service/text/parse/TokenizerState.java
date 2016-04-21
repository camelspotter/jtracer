package net.libcsdbg.jtracer.service.text.parse;

import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

public interface TokenizerState
{
	@NotEmpty
	Property<String> grammar();

	@NotEmpty
	Property<String> input();
}
