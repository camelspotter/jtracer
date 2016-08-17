package net.libcsdbg.jtracer.core;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.annotation.Note;
import net.libcsdbg.jtracer.gui.container.Alert;
import net.libcsdbg.jtracer.gui.container.MainFrame;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.util.UtilityService;
import net.libcsdbg.jtracer.setup.Installer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.Energy4Java;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;

public class ApplicationCore implements AutoInjectable,
                                        WindowListener
{
	protected static ApplicationCore current = null;

	protected static Installer installer = null;

	protected static Logger rootLogger = null;


	@Service
	protected LoggerService loggerSvc;

	@Service
	protected UtilityService utilitySvc;


	protected final Application application;

	protected final Architecture architecture;

	protected final ApplicationProperties properties;


	protected Boolean active;

	protected FileLock globalLock;

	protected MainFrame gui;

	protected GenericUncaughtExceptionHandler uncaughtExceptionHandler;


	public ApplicationCore()
	{
		this(null);
	}

	public ApplicationCore(String propertiesSource)
	{
		active = false;
		properties = new ApplicationProperties(propertiesSource);

		try {
			application = new Energy4Java().newApplication(new Assembler(properties));
			architecture = new Architecture(application.descriptor());
		}
		catch (RuntimeException err) {
			throw err;
		}
		catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}

	public static void attachCurrent(ApplicationCore container)
	{
		current = container;
	}

	public static void detachCurrent()
	{
		current = null;
	}

	public static ApplicationCore getCurrentApplicationCore()
	{
		return current;
	}

	public static Installer getInstaller()
	{
		return installer;
	}

	@Factory(Factory.Type.POJO)
	@Note("The root logger is eagerly initialized")
	public static Logger getRootLogger()
	{
		if (rootLogger == null) {
			rootLogger = LogManager.getRootLogger();
		}

		return rootLogger;
	}

	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	public static void main(String... args)
	{
		installer = new Installer();
		if (installer.isInstallationNeeded()) {
			installer.preInstall();
		}

		ApplicationCore core = null;
		int exitCode = Config.exitSuccess;

		try {
			String propertiesSource = null;
			if (args.length > 0) {
				propertiesSource = args[0].trim();
			}

			core =
				new ApplicationCore(propertiesSource)
					.activate()
					.parseCommandLineArguments(args)
					.registerThreadUncaughtExceptionHandlers()
					.obtainGlobalLock()
					.launchGui();

			/* In sync with windowClosed. The main thread waits until the GUI is closed */
			synchronized (core) {
				while (core.gui != null) {
					try {
						core.wait();
					}
					catch (InterruptedException ignored) {
					}
				}
			}
		}
		catch (Throwable err) {
			getRootLogger().catching(err);
			exitCode = Config.exitFailure;
		}
		finally {
			/* Graceful shutdown */
			try {
				if (core != null && core.active) {
					core.passivate();
				}
			}
			catch (Throwable err) {
				getRootLogger().catching(err);
				exitCode = Config.exitFailure;
			}
		}

		if (installer.isInstallationNeeded()) {
			installer.rollbackPreInstallation();
		}

		System.out.println(Config.farewell);
		System.exit(exitCode);
	}

	/* No synchronization needed during activation */
	protected ApplicationCore activate()
	{
		if (active) {
			return this;
		}

		ApplicationCore.attachCurrent(this);
		selfInject();

		if (installer.isInstallationNeeded()) {
			installer.install()
			         .osSpecificInstallation();
		}

		try {
			application.activate();
			active = true;

			installShutdownHook();

			if (properties.isEnabled("envisage")) {
				architecture.visualize();
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

	public final Application getApplication()
	{
		return application;
	}

	public final ApplicationProperties getApplicationProperties()
	{
		return properties;
	}

	public final Architecture getArchitecture()
	{
		return architecture;
	}

	public final MainFrame getRootFrame()
	{
		return gui;
	}

	@Factory(Factory.Type.POJO)
	@Note("Lazily initialized")
	public GenericUncaughtExceptionHandler getUncaughtExceptionHandler()
	{
		if (uncaughtExceptionHandler == null) {
			uncaughtExceptionHandler = new GenericUncaughtExceptionHandler();
		}

		return uncaughtExceptionHandler;
	}

	protected ApplicationCore installShutdownHook()
	{
		@Note("The only case of logging unrestricted of the dynamic log level")
		Runnable hook = () -> {
			synchronized (this) {
				try {
					if (application == null || !active) {
						return;
					}

					active = false;
					application.passivate();

					getRootLogger().debug("Application '" + application.name() + "' asynchronously passivated");
				}
				catch (Throwable err) {
					getRootLogger().debug("Application '" + application.name() + "' failed to passivate asynchronously");
					getRootLogger().catching(err);
				}
			}
		};

		Thread runner = utilitySvc.fork(hook, Config.shutdownHookName, false);
		Runtime.getRuntime()
		       .addShutdownHook(runner);

		return this;
	}

	protected ApplicationCore launchGui()
	{
		try {
			UIManager.setLookAndFeel(properties.getMandatoryProperty("lnf"));
		}
		catch (Throwable ignored) {
		}

		gui = new MainFrame();
		gui.addWindowListener(this);
		gui.setVisible(true);

		return this;
	}

	protected ApplicationCore obtainGlobalLock()
	{
		try {
			String path = properties.getMandatoryProperty("lock-file");
			File lockFile = utilitySvc.getResource(path);

			globalLock =
				new FileOutputStream(lockFile)
					.getChannel()
					.tryLock();

			if (globalLock == null) {
				Alert.error(null, application.name() + " is already running", true);
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

	protected ApplicationCore parseCommandLineArguments(String... args)
	{
		for (int i = 1; i < args.length; i++) {
			String arg = args[i].trim();

			switch (arg) {
			case "--log-properties":
				properties.logProperties(getRootLogger());
				break;

			case "--dump-properties":
				properties.dumpProperties(System.out);
				break;

			default:
				getRootLogger().warn("Unmapped CLI argument '" + arg + "'");
			}
		}

		return this;
	}

	protected synchronized ApplicationCore passivate()
	{
		if (!active) {
			return this;
		}

		/* Release global lock */
		try {
			if (globalLock != null) {
				globalLock.release();
				globalLock.close();
			}
		}
		catch (Throwable ignored) {
		}

		globalLock = null;
		ApplicationCore.detachCurrent();

		try {
			active = false;
			application.passivate();
			return this;
		}
		catch (RuntimeException err) {
			throw err;
		}
		catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}

	@SuppressWarnings("all")
	protected ApplicationCore registerThreadUncaughtExceptionHandlers()
	{
		ThreadGroup group =
			Thread.currentThread()
			      .getThreadGroup();

		ThreadGroup parent;
		while ((parent = group.getParent()) != null) {
			group = parent;
		}

		Thread[] threads = new Thread[group.activeCount()];
		group.enumerate(threads, true);

		for (Thread t : threads) {
			t.setUncaughtExceptionHandler(getUncaughtExceptionHandler());

			StringBuilder message = new StringBuilder("Uncaught exception handler registered for thread '");
			message.append(t.getName())
			       .append("' (id ")
			       .append(t.getId())
			       .append(", state ")
			       .append(t.getState().name())
			       .append(", group ")
			       .append(t.getThreadGroup().getName())
			       .append(")");

			loggerSvc.info(getClass(), message.toString());
		}

		return this;
	}

	@Override
	public void windowActivated(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
	}

	@Override
	public void windowClosed(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		synchronized (this) {
			gui = null;
			notifyAll();
		}
	}

	@Override
	public void windowClosing(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		if (!Alert.prompt(gui, Config.quitPrompt)) {
			return;
		}

		gui.dispose();
	}

	@Override
	public void windowDeactivated(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
	}

	@Override
	public void windowDeiconified(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
	}

	@Override
	public void windowIconified(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
	}

	@Override
	public void windowOpened(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
	}


	public static class Config
	{
		public static Integer exitFailure = 1;

		public static Integer exitSuccess = 0;


		public static String farewell = "Application complete";

		public static String quitPrompt = "Are you sure you want to quit?";

		public static String shutdownHookName = "Shutdown Hook Thread";
	}

	static {
		rootLogger = LogManager.getRootLogger();
	}
}
