package net.libcsdbg.jtracer.service.util;

public interface PlatformDetectionApi
{
	String getOperatingSystemArchitecture();

	String getOperatingSystemName();

	String getOperatingSystemVersion();

	Platform getPlatform();

	Boolean isLinux();

	Boolean isMac();

	Boolean isOs2();

	Boolean isSolaris();

	Boolean isUnix();

	Boolean isWindows();


	public static enum Platform
	{
		linux,

		mac,

		os2,

		solaris,

		unix,

		windows
	}
}
