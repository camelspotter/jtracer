package net.libcsdbg.jtracer.annotation.constraint;

import org.qi4j.api.constraint.Constraint;

public class WidgetDescriptorConstraint implements Constraint<WidgetDescriptor, String>
{
	@Override
	public boolean isValid(WidgetDescriptor annotation, String value)
	{
		return value != null && value.matches(annotation.grammar());
	}
}
