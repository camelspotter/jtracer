package net.libcsdbg.jtracer.core;

import org.apache.logging.log4j.Logger;
import org.qi4j.api.structure.Application;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

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

		InputStream actual = null;
		try {
			actual = new FileInputStream(source);
		}
		catch (Throwable err) {
			err.printStackTrace();
		}

		try (InputStream istream = (actual != null) ? actual : getClass().getClassLoader().getResourceAsStream(source)) {
			properties.load(istream);
		}
		catch (Throwable err) {
			err.printStackTrace();
		}
	}

	public static void dumpSystemProperties(PrintStream out)
	{
		System.getProperties()
		      .list(out);
	}

	public ApplicationProperties dumpProperties(PrintStream out)
	{
		properties.list(out);
		return this;
	}

	public String getApplicationFullName()
	{
		return getMandatoryProperty(Config.fullNameParam);
	}

	public Application.Mode getApplicationMode()
	{
		if (isContinuousIntegrationBuild()) {
			return Application.Mode.staging;
		}

		if (isReleaseBuild()) {
			return Application.Mode.production;
		}

		if (isTestBuild()) {
			return Application.Mode.test;
		}

		return Application.Mode.development;
	}

	public String getApplicationName()
	{
		return getMandatoryProperty(Config.nameParam);
	}

	public String getApplicationVersion()
	{
		return getMandatoryProperty(Config.versionParam);
	}

	public String getBuildPhase()
	{
		return getMandatoryProperty(Config.buildPhaseParam);
	}

	public String getMandatoryProperty(String property)
	{
		String value = getProperty(property);
		if (value == null) {
			throw new RuntimeException("No configuration found for key '" + property + "'");
		}

		return value;
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

	public Map<String, String> getPropertyMap()
	{
		Map<String, String> retval = new HashMap<>(properties.size());

		properties.stringPropertyNames()
		          .stream()
		          .forEach(k -> retval.put(k, properties.getProperty(k)));

		return retval;
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
				retval.add(((String) value).trim());
			}
			else {
				retval.add(value.toString()
				                .trim());
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

		return
			phase != null &&
			phase.toLowerCase()
			     .equals("ci");
	}

	public Boolean isDevelopmentBuild()
	{
		String phase = getBuildPhase();

		return
			phase != null &&
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
		return
			param.equals("true") ||
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

		return
			phase != null &&
			phase.toLowerCase()
			     .equals("release");
	}

	public Boolean isTestBuild()
	{
		String phase = getBuildPhase();

		return
			phase != null &&
			phase.toLowerCase()
			     .equals("testing");
	}

	public ApplicationProperties logProperties(Logger logger)
	{
		StringBuilder listing = new StringBuilder("Property listing (sourced from '" + source + "') ->");

		getPropertyNames()
			.stream()
			.sorted(String::compareTo)
			.forEach(n -> listing.append("\n\t")
			                     .append(n)
			                     .append(" -> ")
			                     .append(getProperty(n)));

		logger.info(listing.toString());
		return this;
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
