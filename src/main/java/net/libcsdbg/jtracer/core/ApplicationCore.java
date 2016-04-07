package net.libcsdbg.jtracer.core;

import net.libcsdbg.jtracer.component.Alert;
import net.libcsdbg.jtracer.component.MainFrame;
import net.libcsdbg.jtracer.service.registry.RegistryService;
import net.libcsdbg.jtracer.service.utility.UtilityService;
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

public class ApplicationCore implements WindowListener, AutoInjectable
{
	protected static ApplicationCore current = null;

	protected static Logger rootLogger = null;


	@Service
	protected RegistryService registrySvc;

	@Service
	protected UtilityService utilitySvc;


	protected final Application application;

	protected final ApplicationProperties properties;

	protected final Architecture architecture;

	protected final Assembler assembler;

	protected final Energy4Java runtime;


	protected MainFrame gui;

	protected FileLock lock;

	protected Boolean active;


	public ApplicationCore()
	{
		application = null;
		properties = null;
		architecture = null;
		assembler = null;
		runtime = null;
	}

	public ApplicationCore(String propertiesSource)
	{
		active = false;

		properties = new ApplicationProperties(propertiesSource);
		assembler = new Assembler(properties);
		runtime = new Energy4Java();

		try {
			application = runtime.newApplication(assembler);
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

	public static Logger getRootLogger()
	{
		if (rootLogger == null) {
			rootLogger = LogManager.getRootLogger();
		}

		return rootLogger;
	}

	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	public static void main(String[] args)
	{
		ApplicationCore core = null;
		int exitCode = 0;

		try {
			core = new ApplicationCore(null);
			core.activate();

			String path = core.registrySvc.get("lockFile");
			File lockFile = core.utilitySvc.getResource(path);
			FileOutputStream lockStream = new FileOutputStream(lockFile);

			core.lock = lockStream.getChannel().tryLock();
			if (core.lock == null) {
				Alert.error(null, core.registrySvc.get("name") + " is already running", true);
			}

			lockFile.deleteOnExit();
			core.launchGui();

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
			rootLogger.catching(err);
			exitCode = 1;
		}
		finally {
			try {
				if (core != null && core.active) {
					core.passivate();
				}
			}
			catch (RuntimeException err) {
				rootLogger.catching(err);
				exitCode = 1;
			}
		}

		System.out.println("finished");
		System.exit(exitCode);
	}

	/* No synchronization needed during activation */
	public ApplicationCore activate()
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

	public Application getApplication()
	{
		return application;
	}

	public ApplicationProperties getApplicationProperties()
	{
		return properties;
	}

	public Architecture getArchitecture()
	{
		return architecture;
	}

	public Energy4Java getRuntime()
	{
		return runtime;
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

				getRootLogger().debug("Application '" + application.name() + "' asynchronously passivated");
			}
			catch (Throwable err) {
				getRootLogger().catching(err);
			}
		};

		Thread runner = new Thread(hook, "Shutdown Hook Thread");
		runner.setUncaughtExceptionHandler(new GenericUncaughtExceptionHandler());
		Runtime.getRuntime()
		       .addShutdownHook(runner);

		return this;
	}

	public ApplicationCore launchGui()
	{
		try {
			UIManager.setLookAndFeel(properties.getProperty("lnf"));
		}
		catch (Throwable ignored) {
		}

		gui = new MainFrame();
		gui.addWindowListener(this);
		gui.setVisible(true);

		return this;
	}

	public synchronized ApplicationCore passivate()
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

	@Override
	public void windowActivated(WindowEvent event)
	{
		getRootLogger().trace(event.toString());
	}

	@Override
	public synchronized void windowClosed(WindowEvent event)
	{
		getRootLogger().trace(event.toString());

		gui = null;
		notifyAll();
	}

	@Override
	public synchronized void windowClosing(WindowEvent event)
	{
		getRootLogger().trace(event.toString());

		boolean reply = Alert.prompt(gui, "Are you sure you want to quit?");
		if (!reply) {
			return;
		}

		gui.dispose();
	}

	@Override
	public void windowDeactivated(WindowEvent event)
	{
		getRootLogger().trace(event.toString());
	}

	@Override
	public void windowDeiconified(WindowEvent event)
	{
		getRootLogger().trace(event.toString());
	}

	@Override
	public void windowIconified(WindowEvent event)
	{
		getRootLogger().trace(event.toString());
	}

	@Override
	public void windowOpened(WindowEvent event)
	{
		getRootLogger().trace(event.toString());
	}

	static {
		rootLogger = LogManager.getRootLogger();
	}
}
