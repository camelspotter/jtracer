package net.libcsdbg.jtracer.service.util;

import net.libcsdbg.jtracer.component.MainFrame;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.log.LoggerService;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixins(UtilityService.Mixin.class)
@Activators(UtilityService.Activator.class)
public interface UtilityService extends ServiceComposite,
                                        UtilityServiceApi
{
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
		public File createTemporaryDirectory(Boolean autoDelete)
		{
			try {
				String param = registrySvc.get("name");
				if (param == null) {
					param = MainFrame.Config.name;
				}

				File retval =
					Files.createTempDirectory("." + param.trim())
					     .toFile();

				if (autoDelete) {
					retval.deleteOnExit();
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

		@SuppressWarnings("ResultOfMethodCallIgnored")
		@Override
		public List<File> extractJar(File jar, File dir, JProgressBar bar)
		{
			/* List<File> retval = new ArrayList<>(Config.preallocSize);

			if (dir == null) {
				dir = createTemporaryDirectory(true);
			}

			retval.add(dir);
			boolean autoDelete = false;
			if (jar == null) {
				autoDelete = true;
				dir.deleteOnExit();
				jar = new File(System.getProperty("java.class.path"));
			}

			try (JarInputStream in = new JarInputStream(new FileInputStream(jar))) {
				ZipEntry entry;
				float bytes = 0, size = jar.length();
				while ((entry = in.getNextEntry()) != null) {
					File from = new File(entry.getName());
					File to = new File(dir, entry.getName());

					if (entry.isDirectory()) {
						String parts[] = entry.getName().split(File.separator);
						File subDirectory = new File(dir, "");
						if (subDirectory.exists()) {
							continue;
						}

						for (String part : parts) {
							subDirectory = new File(subDirectory, part);
							if (!subDirectory.mkdir()) {
								throw new RuntimeException("Failed to create sub-directory " + subDirectory.getAbsolutePath());
							}

							retval.add(subDirectory);
							bytes += to.length();

							if (bar != null) {
								bar.setValue((int) ((bytes / size) * 100));
							}

							if (autoDelete) {
								subDirectory.deleteOnExit();
							}
						}

						loggerSvc.debug(getClass(), "Created directory " + subDirectory.getAbsolutePath());
						continue;
					}

					loggerSvc.debug(getClass(), from.getAbsolutePath() + " -> " + to.getAbsolutePath());

					if (to.exists()) {
						continue;
					}

					retval.add(to);
					to.createNewFile();
					if (autoDelete) {
						to.deleteOnExit();
					}

					DataOutputStream out = new DataOutputStream(new FileOutputStream(to));
					while (in.available() >= 1) {
						int data = in.read();
						if (data == -1) {
							break;
						}

						out.writeByte(data);
						bytes++;

						if (bar != null) {
							bar.setValue((int) ((bytes / size) * 100));
						}
					}

					out.close();
				}

				if (bar != null) {
					bar.setValue(100);
				}

				in.close();
				return retval;
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			} */

			return new ArrayList<>();
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
			StringBuilder path = new StringBuilder("theme/");

			String theme = registrySvc.get("theme");
			if (theme == null || theme.trim().length() == 0) {
				path.append("default/");
			}
			else {
				path.append(theme.trim())
				    .append("/");
			}

			path.append("icons/")
			    .append(name);

			return new ImageIcon(getResource(path.toString()).getAbsolutePath());
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
