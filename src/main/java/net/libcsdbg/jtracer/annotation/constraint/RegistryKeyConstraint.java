package net.libcsdbg.jtracer.annotation.constraint;

import org.qi4j.api.constraint.Constraint;

public class RegistryKeyConstraint implements Constraint<RegistryKey, String>
{
	private static final long serialVersionUID = -8587521003302059029L;


	@Override
	public boolean isValid(RegistryKey annotation, String value)
	{
		return value != null && value.matches(annotation.grammar());
	}
}
