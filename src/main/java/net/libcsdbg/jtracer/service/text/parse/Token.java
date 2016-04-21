package net.libcsdbg.jtracer.service.text.parse;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.constraints.annotation.NotEmpty;

public interface Token extends ValueComposite
{
	@Optional
	Property<String> delimiter();

	@NotEmpty
	Property<String> text();

	Property<Type> type();


	public static enum Type
	{
		delimiter,

		file,

		function,

		keyword,

		number,

		plain,

		scope,

		type
	}
}
