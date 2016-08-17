package net.libcsdbg.jtracer.setup;

import net.libcsdbg.jtracer.annotation.MixinNote;
import net.libcsdbg.jtracer.gui.container.MainFrame;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.persistence.storage.FileSystemService;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.injection.scope.Service;

import java.io.File;

@MixinNote("Linux/Unix specific installer mixin")
public abstract class UnixLikeInstaller implements PlatformInstaller
{
	@Service
	protected FileSystemService fileSystemSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;

	@Service
	protected UtilityService utilitySvc;


	protected String basename;

	protected String name;

	protected File home;


	@Override
	public PlatformInstaller install()
	{
		loggerSvc.info(getClass(), "Linux/Unix platform installer (platform -> " + platform().get().name() + ")");

		name =
			registrySvc.getOrDefault("name", MainFrame.Config.name)
			           .trim();

		basename = "." + name + "/var/";
		home = fileSystemSvc.getHomeDirectory();

		try {
			installExecutionScript();
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}

		try {
			installDesktopShortcut();
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}

		loggerSvc.info(getClass(), "Linux/Unix platform installer complete");
		return this;
	}

	protected PlatformInstaller installDesktopShortcut()
	{
		File desktop = new File(home, "Desktop");

		/* Check access to the desktop */
		if (!desktop.exists() || !desktop.isDirectory()) {
			throw new RuntimeException("Directory '" + desktop.getAbsolutePath() + "' doesn't exist");
		}

		if (!desktop.canRead() || !desktop.canExecute()) {
			throw new RuntimeException("Directory '" + desktop.getAbsolutePath() + "' isn't accessible");
		}

		String shortcut = name + ".desktop";
		File source = new File(home, basename + shortcut);

		/* Read shortcut source content and process it */
		Byte[] data = fileSystemSvc.read(source);
		String content = new String(unbox(data));
		content = content.replaceAll("~", home.getAbsolutePath());

		/* Save changes to the target shortcut */
		File target = new File(desktop, shortcut);
		fileSystemSvc.save(target, content, false);
		return this;
	}

	protected PlatformInstaller installExecutionScript()
	{
		String source = home.getAbsolutePath() + "/" + basename + name;
		String target = Config.prefix + name;

		/* Copy the script in prefix with root privilege */
		utilitySvc.execute(null, "sudo", false, "cp", source, target);

		/* Make the script executable for all users */
		utilitySvc.execute(null, "sudo", false, "chmod", "a+x", target);

		return this;
	}

	protected byte[] unbox(Byte[] buffer)
	{
		byte[] retval = new byte[buffer.length];
		for (int i = 0; i < buffer.length; i++) {
			retval[i] = buffer[i];
		}

		return retval;
	}


	public static class Config
	{
		public static String prefix = "/usr/bin/";
	}
}
