package net.libcsdbg.jtracer.service.utility;

import net.libcsdbg.jtracer.core.ApplicationCore;
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
import java.util.Arrays;
import java.util.List;

@Mixins(UtilityService.Mixin.class)
@Activators(UtilityService.Activator.class)
public interface UtilityService extends UtilityServiceApi, ServiceComposite
{
	abstract class Mixin implements UtilityService
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
		public UtilityService browse(URL url)
		{
			try {
				String browser = registrySvc.get("browser");
				ProcessBuilder proc = new ProcessBuilder(browser, url.toString());
				proc.start();
				return this;
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
				String commandLine = workingDir + File.separator + executable + " " + String.join(" ", args);

				List<String> cmd = new ArrayList<>();
				cmd.add("." + File.separator + executable);
				cmd.addAll(Arrays.asList(args));

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
			try {
				File retval =
					FileSystemView.getFileSystemView()
					              .getHomeDirectory();

				if (!retval.isDirectory()) {
					throw new RuntimeException("The user home directory '" + retval.getCanonicalPath() + "' doesn't exist");
				}
				else if (!retval.canRead() || !retval.canExecute()) {
					throw new RuntimeException("Can't access the user home directory '" + retval.getCanonicalPath() + "'");
				}

				return retval;
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}

		@Override
		public String getOperatingSystem()
		{
			String os = System.getProperty("os.name");
			if (os == null) {
				return "undefined";
			}

			return os.trim().toLowerCase();
		}

		@Override
		public File getPrefix()
		{
			boolean isDevelopment =
				ApplicationCore.getCurrentApplicationCore()
				               .getApplicationProperties()
				               .isDevelopmentBuild();

			try {
				File retval;
				if (isDevelopment) {
					retval = new File(registrySvc.get("buildPrefix"));
				}
				else if (isLinux()) {
					retval = new File(getHomeDirectory(), ".jTracer");
				}
				else {
					retval = new File("C:\\Program Files\\jTracer");
				}

				if (!retval.isDirectory()) {
					throw new RuntimeException("The installation directory '" + retval.getCanonicalPath() + "' doesn't exist");
				}
				else if (!retval.canRead() || !retval.canExecute()) {
					throw new RuntimeException("Can't access the installation directory '" + retval.getCanonicalPath() + "'");
				}

				return retval;
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}

		@Override
		public List<Image> getProjectIcons()
		{
			List<Image> retval = new ArrayList<>();

			for (int i = 16; i <= 128; i *= 2) {
				ImageIcon icon = loadIcon("icon" + i + ".png");
				if (icon != null) {
					retval.add(icon.getImage());
				}
			}

			return retval;
		}

		@Override
		public File getResource(String path)
		{
			if (isUnixLike()) {
				return new File(getPrefix(), path);
			}

			path = path.replace("/", File.separator);
			return new File(getPrefix(), path);
		}

		@Override
		public Boolean isLinux()
		{
			return getOperatingSystem().matches("linux");
		}

		@Override
		public Boolean isUnixLike()
		{
			return isLinux() || File.separator.equals("/");
		}

		@Override
		public ImageIcon loadIcon(String name)
		{
			/* todo Use 'current' link */
			try {
				File f;
				if (isLinux()) {
					f = getResource("theme/default/icons/" + name);
				}
				else {
					f = getResource("theme/default/icons/" + name);
				}

				return new ImageIcon(f.getCanonicalPath());
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}

		@Override
		public UtilityService mail(URL url)
		{
			try {
				String mailer = registrySvc.get("mailer");
				ProcessBuilder proc = new ProcessBuilder(mailer, url.toString());
				proc.start();
				return this;
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
	}


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
