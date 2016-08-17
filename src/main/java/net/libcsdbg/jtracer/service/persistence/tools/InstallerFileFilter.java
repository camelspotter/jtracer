package net.libcsdbg.jtracer.service.persistence.tools;

import java.io.File;

public abstract class InstallerFileFilter implements Filter
{
	@Override
	public <T> Boolean accept(T entry)
	{
		if (!(entry instanceof File)) {
			throw new IllegalArgumentException("entry (not a java.io.File object)");
		}

		File f = (File) entry;
		String path = f.getPath();

		String separator = File.separator;
		if (separator.equals("\\")) {
			separator = separator.concat("\\");
		}

		for (String dir : Config.acceptedDirectories) {
			if (path.matches("^" + dir + separator + "?.*$")) {
				return true;
			}
		}

		for (String file : Config.acceptedFiles) {
			if (path.equals(file)) {
				return true;
			}
		}

		return false;
	}


	public static class Config
	{
		public static String[] acceptedDirectories = {
			"dictionary",

			"theme",

			"var"
		};

		public static String[] acceptedFiles = {
			"jtracer.properties",

			"log4j.properties",

			"log4j2.xml"
		};
	}
}
