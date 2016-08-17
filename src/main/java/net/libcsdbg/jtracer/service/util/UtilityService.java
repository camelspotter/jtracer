package net.libcsdbg.jtracer.service.util;

import net.libcsdbg.jtracer.gui.container.Alert;
import net.libcsdbg.jtracer.gui.container.MainFrame;
import net.libcsdbg.jtracer.core.ApplicationCore;
import net.libcsdbg.jtracer.core.GenericUncaughtExceptionHandler;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.persistence.storage.FileSystemService;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Mixins({ UtilityService.Mixin.class, PlatformDetector.class })
@Activators(UtilityService.Activator.class)
public interface UtilityService extends ServiceComposite,
                                        UtilityServiceApi
{
	public abstract class Mixin implements UtilityService
	{
		@Service
		protected FileSystemService fileSystemSvc;

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
			String browser = registrySvc.get("browser");

			MainFrame rootFrame =
				ApplicationCore.getCurrentApplicationCore()
				               .getRootFrame();

			try {
				if (browser == null) {
					throw new RuntimeException("No browser configuration found");
				}

				return
					new ProcessBuilder().inheritIO()
					                    .command(browser.trim(), url.toString())
					                    .start();
			}
			catch (RuntimeException err) {
				Alert.error(rootFrame, err.getMessage(), false);
				throw err;
			}
			catch (Throwable err) {
				Alert.error(rootFrame, "Unable to launch browser program '" + browser + "'", false);
				throw new RuntimeException(err);
			}
		}

		@Override
		public Process execute(String workingDir, String executable, Boolean async, String... args)
		{
			try {
				List<String> cmd = new LinkedList<>();

				if (workingDir == null) {
					workingDir = ".";
					cmd.add(executable);
				}
				else {
					cmd.add("." + File.separator + executable);
				}

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
		public Thread fork(Runnable task, String name, Boolean start)
		{
			ThreadGroup group =
				Thread.currentThread()
				      .getThreadGroup();

			GenericUncaughtExceptionHandler handler =
				ApplicationCore.getCurrentApplicationCore()
				               .getUncaughtExceptionHandler();

			Thread retval = new Thread(group, task, name);
			retval.setDaemon(true);
			retval.setUncaughtExceptionHandler(handler);

			if (start) {
				retval.start();
			}

			return retval;
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

			return new File(fileSystemSvc.getResourcePrefix(), path);
		}

		@Override
		public ImageIcon loadIcon(String name)
		{
			StringBuilder path = new StringBuilder("theme/");

			String theme = registrySvc.get("theme");
			if (theme == null || (theme = theme.trim()).length() == 0) {
				path.append("default/");
			}
			else {
				path.append(theme)
				    .append("/");
			}

			path.append("icons/")
			    .append(name);

			return new ImageIcon(getResource(path.toString()).getAbsolutePath());
		}

		@Override
		public Process mailTo(URL url)
		{
			String mailer = registrySvc.get("mailer");

			MainFrame rootFrame =
				ApplicationCore.getCurrentApplicationCore()
				               .getRootFrame();

			try {
				if (mailer == null) {
					throw new RuntimeException("No mailer configuration found");
				}

				return
					new ProcessBuilder().inheritIO()
					                    .command(mailer.trim(), url.toString())
					                    .start();
			}
			catch (RuntimeException err) {
				Alert.error(rootFrame, err.getMessage(), false);
				throw err;
			}
			catch (Throwable err) {
				Alert.error(rootFrame, "Unable to launch mailer program '" + mailer + "'", false);
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


	public static class Activator extends ActivatorAdapter<ServiceReference<UtilityService>>
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
