package net.libcsdbg.jtracer.core;

import net.libcsdbg.jtracer.service.log.LoggerService;
import org.qi4j.api.injection.scope.Service;

import java.util.Arrays;

public class GenericUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler,
                                                        AutoInjectable
{
	@Service
	protected LoggerService loggerSvc;


	public GenericUncaughtExceptionHandler()
	{
		selfInject();
	}

	protected static String tab(Integer depth)
	{
		StringBuilder retval = new StringBuilder(depth + 1);
		retval.append("\n");

		while (depth-- > 0) {
			retval.append("\t");
		}

		return retval.toString();
	}

	@Override
	public void uncaughtException(Thread thrower, Throwable err)
	{
		if (loggerSvc == null) {
			selfInject();
		}

		StringBuilder message = new StringBuilder(Config.preallocSize);
		message.append("Thrown from thread '")
		       .append(thrower.getName())
		       .append("' (")
		       .append(thrower.getId())
		       .append(", ")
		       .append(thrower.getState().name())
		       .append(") ->")

		       .append(tab(1))
		       .append("Class -> ")
		       .append(err.getClass().getName())

		       .append(tab(1))
		       .append("Message -> ")
		       .append(err.getMessage())

		       .append(tab(1))
		       .append("Trace ->");

		Arrays.asList(err.getStackTrace())
		      .stream()
		      .map(StackTraceElement::toString)
		      .forEach(e -> message.append(tab(2))
		                           .append(e));

		loggerSvc.error(getClass(), message.toString());
	}


	public static class Config
	{
		public static Integer preallocSize = 512;
	}
}
