package net.libcsdbg.jtracer.service.component.value;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

public interface GridPresets extends ValueComposite
{
	Property<Integer> baseX();

	Property<Integer> baseY();

	Property<Integer> rowSize();

	Property<Integer> step();
}
