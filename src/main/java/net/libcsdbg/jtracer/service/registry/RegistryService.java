package net.libcsdbg.jtracer.service.registry;

import net.libcsdbg.jtracer.annotation.MixinNote;
import net.libcsdbg.jtracer.annotation.Mutable;
import net.libcsdbg.jtracer.core.ApplicationCore;
import net.libcsdbg.jtracer.core.ApplicationProperties;
import net.libcsdbg.jtracer.service.log.LoggerService;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixins(RegistryService.Mixin.class)
@Activators(RegistryService.Activator.class)
public interface RegistryService extends RegistryServiceApi, ServiceComposite
{
	@Mutable
	@MixinNote("The default service implementation is based on java.util")
	public abstract class Mixin implements RegistryService
	{
		@Service
		protected LoggerService loggerSvc;


		@Override
		public RegistryService activate()
		{
			if (active().get()) {
				return this;
			}

			ApplicationProperties properties =
				ApplicationCore.getCurrentApplicationCore()
				               .getApplicationProperties();

			Map<String, String> map = new HashMap<>(properties.size());
			for (String key : properties.getPropertyNames()) {
				map.put(key, properties.getProperty(key));
			}

			state().set(map);
			source().set(properties.getSource());

			metainfo().set("java.util");
			active().set(true);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' activated (" + metainfo().get() + ")");
			return this;
		}

		@Override
		public RegistryService clear()
		{
			map().clear();
			return this;
		}

		@Override
		public Boolean containsKey(String key)
		{
			return map().containsKey(key);
		}

		@Override
		public Boolean containsValue(String value)
		{
			return map().containsValue(value);
		}

		@Override
		public Set<Map.Entry<String, String>> entrySet()
		{
			return map().entrySet();
		}

		@Override
		public Map<String, String> find(String keyPattern)
		{
			Map<String, String> retval = new HashMap<>();

			keySet().stream()
			        .filter(k -> k.matches(keyPattern))
			        .forEach(k -> retval.put(k, get(k)));

			return retval;
		}

		@Override
		public String get(String key)
		{
			return map().get(key);
		}

		@Override
		public String getOrDefault(String key, String defaultValue)
		{
			return map().getOrDefault(key, defaultValue);
		}

		@Override
		public Boolean isEmpty()
		{
			return map().isEmpty();
		}

		@Override
		public Boolean isEnabled(String key)
		{
			String value = get(key);
			if (value == null) {
				return false;
			}

			value = value.trim().toLowerCase();
			return
				value.equals("true") ||
				value.equals("yes") ||
				value.equals("on") ||
				value.equals("1");
		}

		@Override
		public Set<String> keySet()
		{
			return map().keySet();
		}

		protected Map<String, String> map()
		{
			return state().get();
		}

		@Override
		public RegistryService passivate()
		{
			if (!active().get()) {
				return this;
			}

			clear();
			state().set(null);
			active().set(false);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' passivated (" + metainfo().get() + ")");
			return this;
		}

		@Override
		public String put(String key, String value)
		{
			return map().put(key, value);
		}

		@Override
		public RegistryService putAll(Map<String, String> data)
		{
			map().putAll(data);
			return this;
		}

		@Override
		public String putIfAbsent(String key, String value)
		{
			return map().putIfAbsent(key, value);
		}

		@Override
		public String remove(String key)
		{
			return map().remove(key);
		}

		@Override
		public Boolean remove(String key, String value)
		{
			return map().remove(key, value);
		}

		@Override
		public String replace(String key, String value)
		{
			return map().replace(key, value);
		}

		@Override
		public Boolean replace(String key, String oldValue, String newValue)
		{
			return map().replace(key, oldValue, newValue);
		}

		@Override
		public Integer size()
		{
			return map().size();
		}

		@Override
		public Set<String> values()
		{
			return new HashSet<>(map().values());
		}
	}


	@Mutable(false)
	class Activator extends ActivatorAdapter<ServiceReference<RegistryService>>
	{
		@Override
		public void afterActivation(ServiceReference<RegistryService> svc) throws Exception
		{
			svc.get()
			   .active()
			   .set(false);

			svc.get().activate();
		}

		@Override
		public void beforePassivation(ServiceReference<RegistryService> svc) throws Exception
		{
			svc.get().passivate();
		}
	}
}
