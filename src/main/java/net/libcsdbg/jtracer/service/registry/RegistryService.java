package net.libcsdbg.jtracer.service.registry;

import net.libcsdbg.jtracer.core.ApplicationCore;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.core.ApplicationProperties;
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
import java.util.stream.Collectors;

@Mixins(RegistryService.Mixin.class)
@Activators(RegistryService.Activator.class)
public interface RegistryService extends RegistryServiceApi, ServiceComposite
{
	abstract class Mixin implements RegistryService
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

			metainfo().set("net.libcsdbg.jracer");
			active().set(true);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' activated (" + metainfo().get() + ")");
			return this;
		}

		@Override
		public RegistryService clear()
		{
			state().get()
			       .clear();

			return this;
		}

		@Override
		public Boolean containsKey(String key)
		{
			return
				state().get()
				       .containsKey(key);
		}

		@Override
		public Boolean containsValue(String value)
		{
			return
				state().get()
				       .containsValue(value);
		}

		@Override
		public Set<Map.Entry<String, String>> entrySet()
		{
			return
				state().get()
				       .entrySet();
		}

		@Override
		public Map<String, String> find(String keyPattern)
		{
			Set<String> keys =
				keySet().stream()
				        .filter(k -> k.matches(keyPattern))
				        .collect(Collectors.toSet());

			Map<String, String> retval = new HashMap<>(keys.size());
			keys.stream()
			    .forEach(k -> retval.put(k, get(k)));

			return retval;
		}

		@Override
		public String get(String key)
		{
			return
				state().get()
				       .get(key);
		}

		@Override
		public String getOrDefault(String key, String defaultValue)
		{
			return
				state().get()
				       .getOrDefault(key, defaultValue);
		}

		@Override
		public Boolean isEmpty()
		{
			return
				state().get()
				       .isEmpty();
		}

		@Override
		public Boolean isEnabled(String key)
		{
			String value = get(key);
			if (value == null) {
				return false;
			}

			value = value.toLowerCase().trim();
			return value.equals("true") || value.equals("yes") || value.equals("on");
		}

		@Override
		public Set<String> keySet()
		{
			return
				state().get()
				       .keySet();
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
			return
				state().get()
				       .put(key, value);
		}

		@Override
		public RegistryService putAll(Map<String, String> map)
		{
			state().get()
			       .putAll(map);

			return this;
		}

		@Override
		public String putIfAbsent(String key, String value)
		{
			return
				state().get()
				       .putIfAbsent(key, value);
		}

		@Override
		public String remove(String key)
		{
			return
				state().get()
				       .remove(key);
		}

		@Override
		public Boolean remove(String key, String value)
		{
			return
				state().get()
				       .remove(key, value);
		}

		@Override
		public String replace(String key, String value)
		{
			return
				state().get()
				       .replace(key, value);
		}

		@Override
		public Boolean replace(String key, String oldValue, String newValue)
		{
			return
				state().get()
				       .replace(key, oldValue, newValue);
		}

		@Override
		public Integer size()
		{
			return
				state().get()
				       .size();
		}

		@Override
		public Set<String> values()
		{
			return new HashSet<>(state().get().values());
		}
	}


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
