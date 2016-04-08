package net.libcsdbg.jtracer.service.utility;

import net.libcsdbg.jtracer.annotation.Mutable;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.registry.RegistryService;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* todo Revisit for OS portability */

@Mixins(UtilityService.Mixin.class)
@Activators(UtilityService.Activator.class)
public interface UtilityService extends UtilityServiceApi, ServiceComposite
{
	@Mutable
	public abstract class Mixin implements UtilityService
	{
		@Service
		protected LoggerService loggerSvc;

		@Service
		protected RegistryService registrySvc;


		@Override
		public UtilityService activate()
		{
			if (active().get()) {
				return this;
			}

			metainfo().set("net.libcsdbg.jtracer");
			active().set(true);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' activated (" + metainfo().get() + ")");
			return this;
		}

		@Override
		public Process browse(URL url)
		{
			try {
				String browser = registrySvc.get("browser");
				if (browser == null) {
					throw new RuntimeException("No browser configuration found");
				}

				return
					new ProcessBuilder().inheritIO()
					                    .command(browser.trim(), url.toString())
					                    .start();
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}

		@Override
		public Process execute(String workingDir, String executable, Boolean async, String... args)
		{
			try {
				List<String> cmd = new ArrayList<>();
				cmd.add("." + File.separator + executable);
				if (args.length > 0) {
					Collections.addAll(cmd, args);
				}

				Process proc =
					new ProcessBuilder().inheritIO()
					                    .directory(new File(workingDir))
					                    .command(cmd)
					                    .start();

				if (async) {
					return proc;
				}

				while (true) {
					try {
						proc.waitFor();
						return proc.destroyForcibly();
					}
					catch (InterruptedException ignored) {
					}
				}
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}

		@Override
		public File getHomeDirectory()
		{
			return FileSystemView.getFileSystemView().getHomeDirectory();
		}

		@Override
		public List<Image> getProjectIcons()
		{
			List<Image> retval = new ArrayList<>(Config.iconSizes.length);

			for (Integer size : Config.iconSizes) {
				ImageIcon icon = loadIcon("icon" + size + ".png");
				if (icon != null) {
					retval.add(icon.getImage());
				}
			}

			return retval;
		}

		@Override
		public File getResource(String path)
		{
			if (!File.separator.equals("/")) {
				path = path.replace("/", File.separator);
			}

			return new File(getResourcePrefix(), path);
		}

		@Override
		public File getResourcePrefix()
		{
			try {
				File retval =
					new File(getClass().getProtectionDomain()
					                   .getCodeSource()
					                   .getLocation()
					                   .toURI());

				if (retval.isDirectory()) {
					return retval;
				}

				return retval.getParentFile();
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}

		@Override
		public ImageIcon loadIcon(String name)
		{
			try {
				String iconPath = registrySvc.get("theme");
				if (iconPath == null) {
					iconPath = "theme/default/icons/";
				}
				else {
					iconPath = "theme/" + iconPath.trim() + "/icons/";
				}

				iconPath = getResource(iconPath + name).getCanonicalPath();
				return new ImageIcon(iconPath);
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}

		@Override
		public Process mailTo(URL url)
		{
			try {
				String mailer = registrySvc.get("mailer");
				if (mailer == null) {
					throw new RuntimeException("No mailer configuration found");
				}

				return
					new ProcessBuilder().inheritIO()
					                    .command(mailer.trim(), url.toString())
					                    .start();
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}

		@Override
		public UtilityService passivate()
		{
			if (!active().get()) {
				return this;
			}

			active().set(false);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' passivated (" + metainfo().get() + ")");
			return this;
		}


		public static class Config
		{
			public static Integer[] iconSizes = { 16, 24, 32, 48, 64, 128 };
		}
	}


	@Mutable(false)
	class Activator extends ActivatorAdapter<ServiceReference<UtilityService>>
	{
		@Override
		public void afterActivation(ServiceReference<UtilityService> svc) throws Exception
		{
			svc.get()
			   .active()
			   .set(false);

			svc.get().activate();
		}

		@Override
		public void beforePassivation(ServiceReference<UtilityService> svc) throws Exception
		{
			svc.get().passivate();
		}
	}
}
