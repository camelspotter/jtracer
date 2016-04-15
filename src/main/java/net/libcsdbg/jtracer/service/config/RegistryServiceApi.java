package net.libcsdbg.jtracer.service.config;

import java.util.Map;
import java.util.Set;

public interface RegistryServiceApi extends RegistryServiceState
{
	RegistryService activate();

	RegistryService clear();

	Boolean containsKey(String key);

	Boolean containsValue(String value);

	Set<Map.Entry<String, String>> entrySet();

	Map<String, String> find(String keyPattern);

	String get(String key);

	String getOrDefault(String key, String defaultValue);

	Boolean isEmpty();

	Boolean isEnabled(String key);

	Set<String> keySet();

	RegistryService passivate();

	String put(String key, String value);

	RegistryService putAll(Map<String, String> map);

	String putIfAbsent(String key, String value);

	String remove(String key);

	Boolean remove(String key, String value);

	String replace(String key, String value);

	Boolean replace(String key, String oldValue, String newValue);

	Integer size();

	Set<String> values();
}
