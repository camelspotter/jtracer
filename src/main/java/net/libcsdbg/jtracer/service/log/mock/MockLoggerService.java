package net.libcsdbg.jtracer.service.log.mock;

import net.libcsdbg.jtracer.annotation.MixinNote;
import net.libcsdbg.jtracer.annotation.Note;
import net.libcsdbg.jtracer.service.log.LoggerService;

import java.io.PrintStream;

@MixinNote("Mock service implementation, it logs on console with a simple format, ignoring log levels")
public abstract class MockLoggerService implements LoggerService
{
	protected PrintStream logSink;

	protected PrintStream errorSink;


	@Override
	public LoggerService activate()
	{
		if (active().get()) {
			return this;
		}

		logSink = Config.defaultLogSink;
		errorSink = Config.defaultErrorSink;

		dynamicLogLevel().set(LogLevel.trace);
		metainfo().set("mock");
		mute().set(false);
		active().set(true);

		return info(getClass(), "Service '" + identity().get() + "' activated (" + metainfo().get() + ")");
	}

	@Override
	public LoggerService catching(Class<?> clazz, Throwable err)
	{
		if (!mute().get()) {
			errorSink.println(clazz.getName() + ": ");
			err.printStackTrace(errorSink);
		}

		return this;
	}

	@Note("Fatal exceptions cannot be suppressed")
	@Override
	public LoggerService catchingFatal(Class<?> clazz, Throwable err)
	{
		errorSink.println(clazz.getName() + ": ");
		err.printStackTrace(errorSink);
		return this;
	}

	@Override
	public LoggerService debug(Class<?> clazz, String record)
	{
		if (!mute().get()) {
			logSink.println("[DEBUG] " + clazz.getName() + ": " + record);
		}

		return this;
	}

	@Override
	public LoggerService error(Class<?> clazz, String record)
	{
		if (!mute().get()) {
			errorSink.println("[ERROR] " + clazz.getName() + ": " + record);
		}

		return this;
	}

	@Note("Fatal errors cannot be suppressed")
	@Override
	public LoggerService fatal(Class<?> clazz, String record)
	{
		errorSink.println("[FATAL] " + clazz.getName() + ": " + record);
		return this;
	}

	@Override
	public LoggerService info(Class<?> clazz, String record)
	{
		if (!mute().get()) {
			logSink.println("[INFO] " + clazz.getName() + ": " + record);
		}

		return this;
	}

	@Override
	public Boolean isDynamicLogLevelEnabled(LogLevel level)
	{
		return true;
	}

	@Override
	public Boolean logLevelDown()
	{
		return false;
	}

	@Override
	public Boolean logLevelUp()
	{
		return false;
	}

	@Override
	public LoggerService passivate()
	{
		if (!active().get()) {
			return this;
		}

		info(getClass(), "Service '" + identity().get() + "' passivated (" + metainfo().get() + ")");
		active().set(false);
		logSink = errorSink = null;

		return this;
	}

	@Override
	public LoggerService trace(Class<?> clazz, String record)
	{
		if (!mute().get()) {
			logSink.println("[TRACE] " + clazz.getName() + ": " + record);
		}

		return this;
	}

	@Override
	public LoggerService warning(Class<?> clazz, String record)
	{
		if (!mute().get()) {
			logSink.println("[WARN] " + clazz.getName() + ": " + record);
		}

		return this;
	}


	public static class Config
	{
		public static PrintStream defaultErrorSink = System.err;

		public static PrintStream defaultLogSink = System.out;
	}
}
