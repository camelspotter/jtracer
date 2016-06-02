package net.libcsdbg.jtracer.service.util;

import net.libcsdbg.jtracer.annotation.constraint.Name;
import net.libcsdbg.jtracer.annotation.constraint.NameConstraint;
import net.libcsdbg.jtracer.service.util.tools.Filter;
import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.library.constraints.annotation.URI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.List;

@Constraints(NameConstraint.class)
public interface UtilityServiceApi extends UtilityServiceState
{
	UtilityService activate();

	Process browse(URL url);

	Filter createFilter();

	File createTemporaryDirectory(Boolean autoDelete, String... components);

	Process execute(@URI String workingDir, @Name String executable, Boolean async, String... args);

	List<File> extractJar(@Optional File jar, @Optional File dir, @Optional JProgressBar progressMonitor);

	File getHomeDirectory();

	List<Image> getProjectIcons();

	File getResource(@URI String path);

	File getResourcePrefix();

	Boolean isSelfExecutableJar();

	ImageIcon loadIcon(@URI String name);

	Process mailTo(URL url);

	UtilityService passivate();
}
