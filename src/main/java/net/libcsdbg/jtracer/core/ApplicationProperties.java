package net.libcsdbg.jtracer.core;

import org.qi4j.api.structure.Application;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationProperties
{
	protected final Properties properties;

	protected String source;


	public ApplicationProperties(String source)
	{
		properties = new Properties();

		if (source == null) {
			source = Config.defaultSource;
		}

		this.source = source.trim();
		try (InputStream istream = getClass().getClassLoader().getResourceAsStream(source)) {
			properties.load(istream);
		}
		catch (Throwable ignored) {
		}
	}

	public String getApplicationFullName()
	{
		return getProperty(Config.fullNameParam);
	}

	public Application.Mode getApplicationMode()
	{
		if (isContinuousIntegrationBuild()) {
			return Application.Mode.staging;
		}

		else if (isReleaseBuild()) {
			return Application.Mode.production;
		}

		else if (isTestBuild()) {
			return Application.Mode.test;
		}

		else {
			return Application.Mode.development;
		}
	}

	public String getApplicationName()
	{
		return getProperty(Config.nameParam);
	}

	public String getApplicationVersion()
	{
		return getProperty(Config.versionParam);
	}

	public String getBuildPhase()
	{
		return getProperty(Config.buildPhaseParam);
	}

	public final Properties getProperties()
	{
		return properties;
	}

	public String getProperty(String property)
	{
		property = properties.getProperty(property);
		if (property != null) {
			property = property.trim();
		}

		return property;
	}

	public Set<String> getPropertyNames()
	{
		return
			properties.stringPropertyNames()
			          .stream()
			          .map(String::trim)
			          .collect(Collectors.toSet());
	}

	public Set<String> getPropertyValues()
	{
		Set<String> retval = new HashSet<>(properties.size());
		for (Object value : properties.values()) {
			if (value instanceof String) {
				retval.add(((String) value).trim());
			}
			else {
				retval.add(value.toString().trim());
			}
		}

		return retval;
	}

	public String getSource()
	{
		return source;
	}

	public Boolean isContinuousIntegrationBuild()
	{
		String phase = getBuildPhase();
		return phase != null &&
		       phase.toLowerCase()
		            .equals("ci");
	}

	public Boolean isDevelopmentBuild()
	{
		String phase = getBuildPhase();
		return phase != null &&
		       phase.toLowerCase()
		            .equals("development");
	}

	public Boolean isEnabled(String property)
	{
		String param = getProperty(property);
		if (param == null) {
			return false;
		}

		param = param.toLowerCase();
		return param.equals("true") ||
		       param.equals("yes") ||
		       param.equals("on") ||
		       param.equals("1");
	}

	public Boolean isPropertyEqual(String property, String value)
	{
		String param = getProperty(property);
		return param != null && param.equals(value);
	}

	public Boolean isPropertyMatching(String property, String value)
	{
		String param = getProperty(property);
		return param != null && param.matches(value);
	}

	public Boolean isReleaseBuild()
	{
		String phase = getBuildPhase();
		return phase != null &&
		       phase.toLowerCase()
		            .equals("release");
	}

	public Boolean isTestBuild()
	{
		String phase = getBuildPhase();
		return phase != null &&
		       phase.toLowerCase()
		            .equals("testing");
	}

	public Integer size()
	{
		return properties.size();
	}


	public static class Config
	{
		public static String buildPhaseParam = "build-phase";

		public static String defaultSource = "jtracer.properties";

		public static String fullNameParam = "full-name";

		public static String nameParam = "name";

		public static String versionParam = "version";
	}
}
