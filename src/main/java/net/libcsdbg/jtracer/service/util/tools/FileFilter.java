package net.libcsdbg.jtracer.service.util.tools;

import java.io.File;

public abstract class FileFilter implements Filter
{
	@Override
	public <T> Boolean accept(T entry)
	{
		if (!(entry instanceof File)) {
			throw new IllegalArgumentException("entry (not a java.io.File object");
		}

		File f = (File) entry;
		String path = f.getAbsolutePath();

		if (isSubdirectoryOf(f, "dictionary")) {
			return true;
		}

		if (path.contains("/dictionary/") ||
		    path.contains("/theme/") ||
		    path.contains("/var/")) {
			return true;
		}

		else if (path.endsWith("jtracer.properties") ||
		         path.endsWith("log4j.properties") ||
		         path.endsWith("log4j2.xml")) {
			return true;
		}

		return false;
	}

	protected Boolean isSubdirectoryOf(File file, String dir)
	{
		try {
			String basename;
			if (file.isDirectory()) {
				basename = file.getCanonicalPath();
			}
			else {
				basename = file.getParent();
			}

			return basename.matches("/" + dir + "/?");
		}
		catch (Throwable err) {
			return false;
		}
	}
}
