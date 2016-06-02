package net.libcsdbg.jtracer.service.util.tools;

import net.libcsdbg.jtracer.annotation.MixinNote;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;

@Mixins(Filter.Mixin.class)
public interface Filter extends TransientComposite
{
	<T> Boolean accept(T entry);


	@MixinNote("The default implementation is the mock one")
	public abstract class Mixin implements Filter
	{
		@Override
		public <T> Boolean accept(T entry)
		{
			return false;
		}
	}
}
