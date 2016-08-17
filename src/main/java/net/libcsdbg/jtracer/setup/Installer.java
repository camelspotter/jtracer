package net.libcsdbg.jtracer.setup;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.annotation.Note;
import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.gui.component.ProgressBar;
import net.libcsdbg.jtracer.gui.container.MainFrame;
import net.libcsdbg.jtracer.gui.container.SplashScreen;
import net.libcsdbg.jtracer.service.persistence.jar.JarService;
import net.libcsdbg.jtracer.service.persistence.storage.FileSystemService;
import net.libcsdbg.jtracer.service.persistence.tools.InstallerFileFilter;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import javax.swing.filechooser.FileSystemView;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static net.libcsdbg.jtracer.service.util.PlatformDetectionApi.Platform;

public class Installer implements AutoInjectable
{
	@Structure
	protected Module selfContainer;

	@Service
	protected FileSystemService fileSystemSvc;

	@Service
	protected JarService jarSvc;

	@Service
	protected UtilityService utilitySvc;


	protected final File executable;

	protected final File home;

	protected final File prefix;


	protected final Boolean installationNeeded;

	protected final Boolean installed;

	protected final Boolean selfExecutableJar;


	public Installer()
	{
		File dir =
			FileSystemView.getFileSystemView()
			              .getHomeDirectory();

		if (dir.getAbsolutePath().endsWith("Desktop")) {
			/* Invocation of home.getParent() may return null */
			home = new File(dir.getParent());
		}
		else {
			home = dir;
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

	@Factory(Factory.Type.COMPOSITE)
	public PlatformInstaller detectPlatformInstaller()
	{
		if (selfContainer == null || utilitySvc == null) {
			selfInject();
		}

		TransientBuilder<? extends PlatformInstaller> builder;
		Platform platform = utilitySvc.getPlatform();
		switch (platform) {
		case linux:
		case unix:
		case solaris:
			builder = selfContainer.newTransientBuilder(UnixLikeInstaller.class);
			break;

		case windows:
			builder = selfContainer.newTransientBuilder(WindowsInstaller.class);
			break;

		default:
			builder = selfContainer.newTransientBuilder(PlatformInstaller.Mixin.class);
		}

		builder.prototype()
		       .platform()
		       .set(platform);

		return builder.newInstance();
	}

	public Long estimateInstallationTaskSize()
	{
		if (executable == null) {
			throw new IllegalStateException("No installation context detected");
		}

		if (jarSvc == null) {
			selfInject();
		}

		return executable.length() + jarSvc.getTotalUncompressedSize(executable);
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
		if (!installationNeeded) {
			throw new IllegalStateException(MainFrame.Config.name + " is either already installed or not in production mode");
		}

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
		fileSystemSvc.setResourcePrefix(prefix.getAbsolutePath());

		progressMonitor.setCaption("Installing");
		jarSvc.extractAll(executable, prefix, progressMonitor, InstallerFileFilter.class);

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

	public Installer osSpecificInstallation()
	{
		detectPlatformInstaller().install();
		return this;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Note("The pre-installation is performed first, before application activation, therefore no services and composite factories are available")
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
