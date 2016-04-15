package net.libcsdbg.jtracer.service.log;

import org.qi4j.library.constraints.annotation.NotEmpty;

public interface LoggerServiceApi extends LoggerServiceState
{
	LoggerService activate();

	LoggerService catching(Class<?> clazz, Throwable err);

	LoggerService catchingFatal(Class<?> clazz, Throwable err);

	LoggerService debug(Class<?> clazz, @NotEmpty String record);

	LoggerService error(Class<?> clazz, @NotEmpty String record);

	LoggerService fatal(Class<?> clazz, @NotEmpty String record);

	LoggerService info(Class<?> clazz, @NotEmpty String record);

	Boolean isDynamicLogLevelEnabled(LogLevel level);

	LoggerService logLevelDown();

	LoggerService logLevelUp();

	LoggerService passivate();

	LoggerService trace(Class<?> clazz, @NotEmpty String record);

	LoggerService warning(Class<?> clazz, @NotEmpty String record);
}
