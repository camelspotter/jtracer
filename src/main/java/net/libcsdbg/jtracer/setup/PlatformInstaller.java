package net.libcsdbg.jtracer.setup;

import net.libcsdbg.jtracer.annotation.MixinNote;
import net.libcsdbg.jtracer.service.log.LoggerService;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;

@Mixins(PlatformInstaller.Mixin.class)
public interface PlatformInstaller extends PlatformInstallerState,
                                           TransientComposite
{
	PlatformInstaller install();


	@MixinNote("The default implementation is the mock one")
	public abstract class Mixin implements PlatformInstaller
	{
		@Service
		protected LoggerService loggerSvc;


		@Override
		public PlatformInstaller install()
		{
			String arch =
				platform().get()
				          .name();

			loggerSvc.info(getClass(), "Mock platform installer (platform -> " + arch + ")");
			return this;
		}
	}
}
