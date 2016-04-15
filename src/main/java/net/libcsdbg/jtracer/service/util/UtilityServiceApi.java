package net.libcsdbg.jtracer.service.util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.List;

public interface UtilityServiceApi extends UtilityServiceState
{
	UtilityService activate();

	Process browse(URL url);

	Process execute(String workingDir, String executable, Boolean async, String... args);

	File getHomeDirectory();

	List<Image> getProjectIcons();

	File getResource(String path);

	File getResourcePrefix();

	ImageIcon loadIcon(String name);

	Process mailTo(URL url);

	UtilityService passivate();
}
