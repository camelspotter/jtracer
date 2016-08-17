package net.libcsdbg.jtracer.service.util;

public abstract class PlatformDetector implements UtilityService
{
	protected String osArch;

	protected String osName;

	protected String osVersion;


	@Override
	public String getOperatingSystemArchitecture()
	{
		if (osArch != null) {
			return osArch;
		}

		String key = "os.arch";
		osArch = System.getProperty(key);
		if (osArch == null) {
			throw new IllegalStateException("Mandatory system property '" + key + "' missing");
		}

		return osArch;
	}

	@Override
	public String getOperatingSystemName()
	{
		if (osName != null) {
			return osName;
		}

		String key = "os.name";
		osName = System.getProperty(key);
		if (osName == null) {
			throw new IllegalStateException("Mandatory system property '" + key + "' missing");
		}

		return osName;
	}

	@Override
	public String getOperatingSystemVersion()
	{
		if (osVersion != null) {
			return osVersion;
		}

		String key = "os.version";
		osVersion = System.getProperty(key);
		if (osVersion == null) {
			throw new IllegalStateException("Mandatory system property '" + key + "' missing");
		}

		return osVersion;
	}

	@Override
	public Platform getPlatform()
	{
		if (isLinux()) {
			return Platform.linux;
		}

		else if (isMac()) {
			return Platform.mac;
		}

		else if (isOs2()) {
			return Platform.os2;
		}

		else if (isSolaris()) {
			return Platform.solaris;
		}

		else if (isUnix()) {
			return Platform.unix;
		}

		else if (isWindows()) {
			return Platform.windows;
		}

		throw new RuntimeException("Failed to resolve host Operating System");
	}

	@Override
	public Boolean isLinux()
	{
		return
			getOperatingSystemName().toLowerCase()
			                        .startsWith("linux");
	}

	@Override
	public Boolean isMac()
	{
		String name = getOperatingSystemName().toLowerCase();
		return name.startsWith("mac") || name.startsWith("darwin");
	}

	@Override
	public Boolean isOs2()
	{
		return
			getOperatingSystemName().toLowerCase()
			                        .startsWith("os/2");
	}

	@Override
	public Boolean isSolaris()
	{
		String name = getOperatingSystemName().toLowerCase();
		return name.startsWith("solaris") || name.contains("sunos");
	}

	@Override
	public Boolean isUnix()
	{
		String name = getOperatingSystemName().toLowerCase();
		for (String flavour : Config.unixFlavours) {
			if (name.contains(flavour)) {
				return true;
			}
		}

		return isSolaris();
	}

	@Override
	public Boolean isWindows()
	{
		return
			getOperatingSystemName().toLowerCase()
			                        .startsWith("windows");
	}


	public static class Config
	{
		public static String[] unixFlavours = {
			"aix",

			"digital unix",

			"freebsd",

			"hp-ux",

			"irix",

			"mpe/ix",

			"netbsd",

			"openbsd"
		};
	}
}
