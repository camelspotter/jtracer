package net.libcsdbg.jtracer.service.log.mock;

import net.libcsdbg.jtracer.service.log.LoggerService;

import java.io.PrintStream;

public abstract class MockLoggerService implements LoggerService
{
	protected PrintStream stream;


	@Override
	public LoggerService activate()
	{
		if (active().get()) {
			return this;
		}

		stream = System.out;

		metainfo().set("mock");
		mute().set(false);
		active().set(true);

		return info(getClass(), "Service '" + identity().get() + "' activated (" + metainfo().get() + ")");
	}

	@Override
	public LoggerService catching(Class<?> clazz, Throwable err)
	{
		if (!mute().get()) {
			stream.println(clazz.getName() + ": ");
			err.printStackTrace(stream);
		}

		return this;
	}

	@Override
	public LoggerService debug(Class<?> clazz, String record)
	{
		if (!mute().get()) {
			stream.println("[DEBUG] " + clazz.getName() + ": " + record);
		}

		return this;
	}

	@Override
	public LoggerService error(Class<?> clazz, String record)
	{
		if (!mute().get()) {
			stream.println("[ERROR] " + clazz.getName() + ": " + record);
		}

		return this;
	}

	@Override
	public LoggerService fatal(Class<?> clazz, String record)
	{
		if (!mute().get()) {
			stream.println("[FATAL] " + clazz.getName() + ": " + record);
		}

		return this;
	}

	@Override
	public LoggerService info(Class<?> clazz, String record)
	{
		if (!mute().get()) {
			stream.println("[INFO] " + clazz.getName() + ": " + record);
		}

		return this;
	}

	@Override
	public LoggerService passivate()
	{
		if (!active().get()) {
			return this;
		}

		info(getClass(), "Service '" + identity().get() + "' passivated (" + metainfo().get() + ")");
		active().set(false);
		stream = null;

		return this;
	}

	@Override
	public LoggerService trace(Class<?> clazz, String record)
	{
		if (!mute().get()) {
			stream.println("[TRACE] " + clazz.getName() + ": " + record);
		}

		return this;
	}

	@Override
	public LoggerService warning(Class<?> clazz, String record)
	{
		if (!mute().get()) {
			stream.println("[WARN] " + clazz.getName() + ": " + record);
		}

		return this;
	}
}
