package net.libcsdbg.jtracer.annotation.constraint;

import org.qi4j.api.constraint.Constraint;

public class WidgetDescriptorConstraint implements Constraint<WidgetDescriptor, String>
{
	private static final long serialVersionUID = 6036812649278651669L;


	@Override
	public boolean isValid(WidgetDescriptor annotation, String value)
	{
		return value != null && value.matches(annotation.grammar());
	}
}
