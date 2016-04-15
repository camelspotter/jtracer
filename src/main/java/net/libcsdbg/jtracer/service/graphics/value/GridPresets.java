package net.libcsdbg.jtracer.service.graphics.value;

import net.libcsdbg.jtracer.annotation.Note;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.constraints.annotation.Range;

@Note("Coordinates are ranged with respect to the VGA resolution (640, 480)")
public interface GridPresets extends ValueComposite
{
	@Range(min = 10, max = 320)
	Property<Integer> baseX();

	@Range(min = 10, max = 240)
	Property<Integer> baseY();

	@Range(min = 5, max = 25)
	Property<Integer> rowSize();

	@Range(min = 10, max = 240)
	Property<Integer> step();
}
