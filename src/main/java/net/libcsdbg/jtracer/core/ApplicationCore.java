package net.libcsdbg.jtracer.core;

import net.libcsdbg.jtracer.annotation.Note;
import net.libcsdbg.jtracer.component.Alert;
import net.libcsdbg.jtracer.component.MainFrame;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.util.UtilityService;
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

	@Note("Root logger is eagerly initialized")
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
		Runnable hook = () -> {
			try {
				synchronized (this) {
					if (application == null || !active) {
						return;
					}

					application.passivate();
					active = false;
				}

				/* The only case of log unrestricted of the dynamic log level */
				getRootLogger().debug("Application '" + application.name() + "' asynchronously passivated");
			}
			catch (Throwable err) {
				getRootLogger().catching(err);
			}
		};

		ThreadGroup group =
			Thread.currentThread()
			      .getThreadGroup();

		Thread runner = new Thread(group, hook, Config.shutdownHookName);
		runner.setUncaughtExceptionHandler(getUncaughtExceptionHandler());

		Runtime.getRuntime()
		       .addShutdownHook(runner);

		return this;
	}

	protected ApplicationCore launchGui()
	{
		try {
			UIManager.setLookAndFeel(properties.getProperty("lnf"));
			Thread.sleep(Config.startupDelay);
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
			String path = properties.getProperty("lock-file");
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

	protected synchronized ApplicationCore passivate()
	{
		if (!active) {
			return this;
		}

		ApplicationCore.detachCurrent();

		try {
			application.passivate();
			active = false;
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

		for (Thread thread : threads) {
			thread.setUncaughtExceptionHandler(getUncaughtExceptionHandler());

			StringBuilder message = new StringBuilder("Uncaught exception handler registered for thread '");
			message.append(thread.getName())
			       .append("' (id ")
			       .append(thread.getId())
			       .append(", group ")
			       .append(thread.getThreadGroup().getName())
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
	public synchronized void windowClosed(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		gui = null;
		notifyAll();
	}

	@Override
	public synchronized void windowClosing(WindowEvent event)
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

		public static Integer startupDelay = 2500;


		public static String farewell = "Application complete";

		public static String quitPrompt = "Are you sure you want to quit?";

		public static String shutdownHookName = "Shutdown Hook Thread";
	}

	static {
		rootLogger = LogManager.getRootLogger();
	}
}
