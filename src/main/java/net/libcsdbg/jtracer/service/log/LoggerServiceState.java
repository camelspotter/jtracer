package net.libcsdbg.jtracer.service.log;

import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

public interface LoggerServiceState
{
	Property<Boolean> active();

	Property<LogLevel> dynamicLogLevel();

	@NotEmpty
	Property<String> metainfo();

	Property<Boolean> mute();


	public static enum LogLevel
	{
		trace,

		debug,

		info,

		warning,

		error,

		fatal
	}
}
