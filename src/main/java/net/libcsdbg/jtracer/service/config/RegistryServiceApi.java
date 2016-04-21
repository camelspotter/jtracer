package net.libcsdbg.jtracer.service.config;

import net.libcsdbg.jtracer.annotation.constraint.Name;
import net.libcsdbg.jtracer.annotation.constraint.NameConstraint;
import net.libcsdbg.jtracer.annotation.constraint.RegistryKey;
import net.libcsdbg.jtracer.annotation.constraint.RegistryKeyConstraint;
import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Constraints;

import java.util.Map;
import java.util.Set;

@Constraints({ NameConstraint.class, RegistryKeyConstraint.class })
public interface RegistryServiceApi extends RegistryServiceState
{
	RegistryService activate();

	RegistryService clear();

	Boolean containsKey(@RegistryKey String key);

	Boolean containsValue(String value);

	Set<Map.Entry<String, String>> entrySet();

	Map<String, String> find(@Name String keyPattern);

	String get(@RegistryKey String key);

	String getOrDefault(@RegistryKey String key, @Optional String defaultValue);

	Boolean isEmpty();

	Boolean isEnabled(@RegistryKey String key);

	Set<String> keySet();

	RegistryService passivate();

	String put(@RegistryKey String key, String value);

	RegistryService putAll(Map<String, String> map);

	String putIfAbsent(@RegistryKey String key, String value);

	String remove(@RegistryKey String key);

	Boolean remove(@RegistryKey String key, String value);

	String replace(@RegistryKey String key, String value);

	Boolean replace(@RegistryKey String key, String oldValue, String newValue);

	Integer size();

	Set<String> values();
}
