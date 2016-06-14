package net.libcsdbg.jtracer.service.persistence.tools;

import java.io.File;

public abstract class FileFilter implements Filter
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

		for (String dir : Config.directories) {
			if (path.matches("^" + dir + separator + "?.*$")) {
				return true;
			}
		}

		for (String file : Config.files) {
			if (path.equals(file)) {
				return true;
			}
		}

		return false;
	}


	public static class Config
	{
		public static String[] directories = {
			"dictionary",
			"theme",
			"var"
		};

		public static String[] files = {
			"jtracer.properties",
			"log4j.properties",
			"log4j2.xml"
		};
	}
}
