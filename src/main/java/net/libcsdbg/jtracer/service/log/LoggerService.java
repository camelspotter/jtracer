package net.libcsdbg.jtracer.service.log;

import net.libcsdbg.jtracer.annotation.MixinNote;
import net.libcsdbg.jtracer.annotation.Mutable;
import net.libcsdbg.jtracer.core.ApplicationCore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;

@Mixins(LoggerService.Mixin.class)
@Activators(LoggerService.Activator.class)
public interface LoggerService extends LoggerServiceApi, ServiceComposite
{
	@Mutable
	@MixinNote("The default service implementation uses log4j2")
	public abstract class Mixin implements LoggerService
	{
		protected Logger rootLogger;


		@Override
		public LoggerService activate()
		{
			if (active().get()) {
				return this;
			}

			rootLogger = ApplicationCore.getRootLogger();
			if (rootLogger == null) {
				throw new RuntimeException("Root logger unattainable");
			}

			metainfo().set("org.apache.logging.log4j");
			mute().set(false);
			active().set(true);

			return info(getClass(), "Service '" + identity().get() + "' activated (" + metainfo().get() + ")");
		}

		@Override
		public LoggerService catching(Class<?> clazz, Throwable err)
		{
			if (mute().get()) {
				return this;
			}

			Logger current = getLoggerForClass(clazz);
			if (current.isErrorEnabled()) {
				current.catching(err);
			}

			return this;
		}

		@Override
		public LoggerService debug(Class<?> clazz, String record)
		{
			if (mute().get()) {
				return this;
			}

			Logger current = getLoggerForClass(clazz);
			if (current.isDebugEnabled()) {
				current.debug(record);
			}

			return this;
		}

		@Override
		public LoggerService error(Class<?> clazz, String record)
		{
			if (mute().get()) {
				return this;
			}

			Logger current = getLoggerForClass(clazz);
			if (current.isErrorEnabled()) {
				current.error(record);
			}

			return this;
		}

		@Override
		public LoggerService fatal(Class<?> clazz, String record)
		{
			if (mute().get()) {
				return this;
			}

			Logger current = getLoggerForClass(clazz);
			if (current.isFatalEnabled()) {
				current.fatal(record);
			}

			return this;
		}

		protected Logger getLoggerForClass(Class<?> clazz)
		{
			Logger retval = LogManager.getLogger(clazz);
			if (retval == null) {
				return rootLogger;
			}

			return retval;
		}

		@Override
		public LoggerService info(Class<?> clazz, String record)
		{
			if (mute().get()) {
				return this;
			}

			Logger current = getLoggerForClass(clazz);
			if (current.isInfoEnabled()) {
				current.info(record);
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
			rootLogger = null;

			return this;
		}

		@Override
		public LoggerService trace(Class<?> clazz, String record)
		{
			if (mute().get()) {
				return this;
			}

			Logger current = getLoggerForClass(clazz);
			if (current.isTraceEnabled()) {
				current.trace(record);
			}

			return this;
		}

		@Override
		public LoggerService warning(Class<?> clazz, String record)
		{
			if (mute().get()) {
				return this;
			}

			Logger current = getLoggerForClass(clazz);
			if (current.isWarnEnabled()) {
				current.warn(record);
			}

			return this;
		}
	}


	@Mutable(false)
	class Activator extends ActivatorAdapter<ServiceReference<LoggerService>>
	{
		@Override
		public void afterActivation(ServiceReference<LoggerService> svc) throws Exception
		{
			svc.get()
			   .active()
			   .set(false);

			svc.get().activate();
		}

		@Override
		public void beforePassivation(ServiceReference<LoggerService> svc) throws Exception
		{
			svc.get().passivate();
		}
	}
}
