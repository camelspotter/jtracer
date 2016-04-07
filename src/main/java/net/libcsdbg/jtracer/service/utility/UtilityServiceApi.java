package net.libcsdbg.jtracer.service.utility;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.List;

public interface UtilityServiceApi extends UtilityServiceState
{
	UtilityService activate();

	UtilityService browse(URL url);

	Process execute(String workingDir, String executable, Boolean async, String... args);

	File getHomeDirectory();

	String getOperatingSystem();

	File getPrefix();

	List<Image> getProjectIcons();

	File getResource(String path);

	Boolean isLinux();

	Boolean isUnixLike();

	ImageIcon loadIcon(String name);

	UtilityService mail(URL url);

	UtilityService passivate();
}
