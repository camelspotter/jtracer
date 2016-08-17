package net.libcsdbg.jtracer.setup;

import net.libcsdbg.jtracer.annotation.MixinNote;
import net.libcsdbg.jtracer.service.log.LoggerService;
import org.qi4j.api.injection.scope.Service;

@MixinNote("Windows specific installer mixin")
public abstract class WindowsInstaller implements PlatformInstaller
{
	@Service
	protected LoggerService loggerSvc;


	@Override
	public PlatformInstaller install()
	{
		String arch =
			platform().get()
			          .name();

		loggerSvc.info(getClass(), "Windows platform installer (platform -> " + arch + ")");
		return this;
	}
}
