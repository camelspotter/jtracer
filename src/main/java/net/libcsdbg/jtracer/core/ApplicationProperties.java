package net.libcsdbg.jtracer.core;

import org.qi4j.api.structure.Application;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class ApplicationProperties
{
	protected final Properties properties;

	protected String source;


	public ApplicationProperties()
	{
		properties = new Properties();
	}

	public ApplicationProperties(String source)
	{
		properties = new Properties();

		if (source == null) {
			source = Config.defaultSource;
		}

		this.source = source;
		try (InputStream istream = getClass().getClassLoader().getResourceAsStream(source)) {
			properties.load(istream);
		}
		catch (Throwable ignored) {
		}
	}

	public Application.Mode getApplicationMode()
	{
		if (isTestBuild()) {
			return Application.Mode.test;
		}

		else if (isContinuousIntegrationBuild()) {
			return Application.Mode.staging;
		}

		else if (isReleaseBuild()) {
			return Application.Mode.production;
		}

		else {
			return Application.Mode.development;
		}
	}

	public String getApplicationVersion()
	{
		return getProperty(Config.versionParam);
	}

	public String getProfile()
	{
		return getProperty(Config.profileParam);
	}

	public Properties getProperties()
	{
		return properties;
	}

	public String getProperty(String property)
	{
		return properties.getProperty(property);
	}

	public Set<String> getPropertyNames()
	{
		return properties.stringPropertyNames();
	}

	public Set<String> getPropertyValues()
	{
		Set<String> retval = new HashSet<>(properties.size());
		for (Object value : properties.values()) {
			if (value instanceof String) {
				retval.add((String) value);
			}
			else {
				retval.add(value.toString());
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
		String profile = getProfile();
		return profile != null && profile.matches(Config.ciProfilePattern);
	}

	public Boolean isDevelopmentBuild()
	{
		String profile = getProfile();
		return profile != null && profile.matches(Config.developmentProfilePattern);
	}

	public Boolean isEnabled(String property)
	{
		String param = getProperty(property);
		if (param == null) {
			return false;
		}

		param = param.toLowerCase();
		return param.equals("true") || param.equals("yes") || param.equals("on");
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
		String profile = getProfile();
		return profile != null && profile.matches(Config.releaseProfilePattern);
	}

	public Boolean isTestBuild()
	{
		String profile = getProfile();
		return profile != null && profile.matches(Config.testingProfilePattern);
	}

	public Integer size()
	{
		return properties.size();
	}


	public static class Config
	{
		/* Generic */

		public static String defaultSource = "jtracer.properties";

		public static String profileParam = "profileName";

		public static String versionParam = "version";


		/* Profile name patterns */

		public static String ciProfilePattern = "^ci\\-[a-zA-Z0-9]+$";

		public static String developmentProfilePattern = "^development\\-[a-zA-Z0-9]+$";

		public static String releaseProfilePattern = "^release\\-[a-zA-Z0-9]+$";

		public static String testingProfilePattern = "^testing[0-9]+$";
	}
}
