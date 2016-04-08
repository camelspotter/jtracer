package net.libcsdbg.jtracer.service.log;

public interface LoggerServiceApi extends LoggerServiceState
{
	LoggerService activate();

	LoggerService catching(Class<?> clazz, Throwable err);

	LoggerService debug(Class<?> clazz, String record);

	LoggerService error(Class<?> clazz, String record);

	LoggerService fatal(Class<?> clazz, String record);

	LoggerService info(Class<?> clazz, String record);

	LoggerService passivate();

	LoggerService trace(Class<?> clazz, String record);

	LoggerService warning(Class<?> clazz, String record);
}
