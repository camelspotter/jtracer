package net.libcsdbg.jtracer.annotation.constraint;

import org.qi4j.api.constraint.Constraint;

public class NameConstraint implements Constraint<Name, String>
{
	private static final long serialVersionUID = -6613535648301519947L;


	@Override
	public boolean isValid(Name annotation, String value)
	{
		return value != null && value.matches(annotation.grammar());
	}
}
