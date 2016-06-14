package net.libcsdbg.jtracer.core;

import net.libcsdbg.jtracer.component.MainFrame;
import net.libcsdbg.jtracer.component.ProgressBar;
import net.libcsdbg.jtracer.component.Splash;
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

	public Installer deleteRecursively(File dismissed)
	{
		if (!dismissed.isDirectory()) {
			return deleteSingle(dismissed);
		}

		File[] listing = dismissed.listFiles();
		if (listing == null || listing.length == 0) {
			return deleteSingle(dismissed);
		}

		for (File f : listing) {
			if (f.isDirectory()) {
				deleteRecursively(f);
			}
			else {
				deleteSingle(f);
			}
		}

		return deleteSingle(dismissed);
	}

	public Installer deleteSingle(File dismissed)
	{
		if (!dismissed.exists()) {
			return this;
		}

		else if (!dismissed.delete()) {
			dismissed.deleteOnExit();
		}

		return this;
	}

	public Long estimateInstallationTaskSize()
	{
		if (jarSvc == null) {
			selfInject();
		}

		return executable.length() + jarSvc.getTotalUncompressedSize(executable);
	}

	public Installer finalizeInstallation()
	{
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

	public Installer install()
	{
		selfInject();

		if (!prefix.mkdirs()) {
			throw new RuntimeException("Failed to create the installation directory '" + prefix.getAbsolutePath() + "'");
		}

		Long taskSize = estimateInstallationTaskSize();

		Splash splash = new Splash();
		ProgressBar progressMonitor = splash.getProgress();
		progressMonitor.setMinimum(0);
		progressMonitor.setMaximum(taskSize.intValue());

		progressMonitor.setCaption("Copying");
		File copied = new File(prefix, executable.getName());
		fileSystemSvc.copy(executable, copied, progressMonitor);

		progressMonitor.setCaption("Installing");
		jarSvc.extractAll(executable, prefix, progressMonitor);

		try {
			loggerSvc.debug(getClass(), progressMonitor.getValue() + " ~= " + taskSize);

			progressMonitor.setCaption("Finalizing");
			progressMonitor.setValue(taskSize.intValue());

			Thread.sleep(Config.installationFinalizingDelay);
		}
		catch (InterruptedException ignored) {
		}

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

		String jarPath = executable.getAbsolutePath();
		try (JarFile jar = new JarFile(jarPath, true)) {
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
		for (String path : Config.preInstallationFiles) {
			File target = new File(path);

			while (target != null) {
				deleteSingle(target);
				target = target.getParentFile();
			}
		}

		for (String path : Config.scannedForRollback) {
			File target = new File(path);

			if (target.isDirectory()) {
				deleteRecursively(target);
			}
			else {
				deleteSingle(target);
			}
		}

		return this;
	}


	public static class Config
	{
		public static Integer installationFinalizingDelay = 2000;

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
