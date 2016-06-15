package net.libcsdbg.jtracer.core;

import net.libcsdbg.jtracer.component.MainFrame;
import net.libcsdbg.jtracer.component.ProgressBar;
import net.libcsdbg.jtracer.component.SplashScreen;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.persistence.jar.JarService;
import net.libcsdbg.jtracer.service.persistence.storage.FileSystemService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.filechooser.FileSystemView;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Installer implements AutoInjectable
{
	@Service
	protected FileSystemService fileSystemSvc;

	@Service
	protected JarService jarSvc;

	@Service
	protected LoggerService loggerSvc;


	protected File executable;

	protected File home;

	protected File prefix;


	protected Boolean installationNeeded;

	protected Boolean installed;

	protected Boolean selfExecutableJar;


	public Installer()
	{
		home =
			FileSystemView.getFileSystemView()
			              .getHomeDirectory();

		if (home.getAbsolutePath().endsWith("Desktop")) {
			/* The call to home.getParentFile() may return null */
			home = new File(home.getParent());
		}

		prefix = new File(home, "." + MainFrame.Config.name);

		installed =
			prefix.exists() &&
			prefix.canRead() &&
			prefix.canExecute() &&
			prefix.isDirectory();

		String classpath = System.getProperty("java.class.path");
		String invocator = System.getProperty("sun.java.command");
		selfExecutableJar = classpath.equals(invocator);

		executable = (selfExecutableJar) ? new File(classpath) : null;

		installationNeeded = !installed && selfExecutableJar;
	}

	public Long estimateInstallationTaskSize()
	{
		if (jarSvc == null) {
			selfInject();
		}

		return executable.length() + jarSvc.getTotalUncompressedSize(executable);
	}

	/* todo Perform final, platform-specific installation steps (shortcuts, links, configurations e.t.c) */
	public Installer finalizeInstallation()
	{
		File src = new File(prefix, "var/jTracer-1.04.desktop");
		File dst = new File(home, "Desktop/jTracer-1.04.desktop");

		if (fileSystemSvc == null) {
			selfInject();
		}

		loggerSvc.debug(getClass(), "Copying '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath() + "'");
		try {
			fileSystemSvc.copy(src, dst, null);
		}
		catch (Throwable err) {
			loggerSvc.error(getClass(), "Failed copying '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath() + "'");
			loggerSvc.catching(getClass(), err);
		}

		return this;
	}

	public final File getExecutable()
	{
		return executable;
	}

	public final File getHome()
	{
		return home;
	}

	public final File getPrefix()
	{
		return prefix;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public Installer install()
	{
		selfInject();

		prefix.mkdirs();
		if (!fileSystemSvc.isAccessibleDirectory(prefix)) {
			throw new RuntimeException("The installation directory '" + prefix.getAbsolutePath() + "' doesn't exist or isn't accessible");
		}

		SplashScreen splash = new SplashScreen();
		ProgressBar progressMonitor = splash.getProgress();
		progressMonitor.setMinimum(0);
		progressMonitor.setMaximum(estimateInstallationTaskSize().intValue());

		progressMonitor.setCaption("Copying");
		fileSystemSvc.copy(executable, new File(prefix, executable.getName()), progressMonitor);

		progressMonitor.setCaption("Installing");
		jarSvc.extractAll(executable, prefix, progressMonitor);

		progressMonitor.setCaption("Finalizing")
		               .complete(Config.finalizingDelay);

		splash.setVisible(false);
		return this;
	}

	public final Boolean isInstallationNeeded()
	{
		return installationNeeded;
	}

	public final Boolean isInstalled()
	{
		return installed;
	}

	public final Boolean isSelfExecutableJar()
	{
		return selfExecutableJar;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public Installer preInstall()
	{
		if (!installationNeeded) {
			throw new IllegalStateException(MainFrame.Config.name + " is either already installed or not in production mode");
		}

		File logsPrefix = new File("logs");
		logsPrefix.mkdirs();
		logsPrefix.deleteOnExit();

		try (JarFile jar = new JarFile(executable.getAbsolutePath(), true)) {
			for (String name : Config.preInstallationFiles) {
				JarEntry entry = jar.getJarEntry(name);
				if (entry == null) {
					continue;
				}

				File to = new File(name);
				File prefix = (entry.isDirectory()) ? to : to.getParentFile();
				prefix.mkdirs();
				while (prefix != null) {
					prefix.deleteOnExit();
					prefix = prefix.getParentFile();
				}

				if (entry.isDirectory()) {
					continue;
				}

				to.deleteOnExit();
				try (InputStream in = jar.getInputStream(entry);
				     DataOutputStream out = new DataOutputStream(new FileOutputStream(to))) {
					while (in.available() > 0) {
						int data = in.read();
						if (data == -1) {
							break;
						}

						out.writeByte(data);
					}
				}
				catch (RuntimeException err) {
					throw err;
				}
				catch (Throwable err) {
					throw new RuntimeException(err);
				}
			}

			return this;
		}
		catch (RuntimeException err) {
			throw err;
		}
		catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}

	public Installer rollbackPreInstallation()
	{
		if (fileSystemSvc == null) {
			selfInject();
		}

		for (String path : Config.preInstallationFiles) {
			File target = new File(path);

			while (target != null) {
				fileSystemSvc.deleteSingle(target);
				target = target.getParentFile();
			}
		}

		for (String path : Config.scannedForRollback) {
			fileSystemSvc.deleteRecursively(new File(path));
		}

		return this;
	}


	public static class Config
	{
		public static Integer finalizingDelay = 2000;

		public static String[] preInstallationFiles = {
			"theme/common/icon.png",
			"theme/common/splash.css",
			"theme/common/splash.html",
		};

		public static String[] scannedForRollback = {
			"logs"
		};
	}
}
