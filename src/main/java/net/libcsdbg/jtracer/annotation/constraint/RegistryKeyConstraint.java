package net.libcsdbg.jtracer.annotation.constraint;

import org.qi4j.api.constraint.Constraint;

public class RegistryKeyConstraint implements Constraint<RegistryKey, String>
{
	@Override
	public boolean isValid(RegistryKey annotation, String value)
	{
		return value != null && value.matches("^[a-zA-Z0-9_]+(-[a-zA-Z0-9_]+)*$");
	}
}
