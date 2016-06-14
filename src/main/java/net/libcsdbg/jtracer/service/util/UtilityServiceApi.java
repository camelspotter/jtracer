package net.libcsdbg.jtracer.service.util;

import net.libcsdbg.jtracer.annotation.constraint.Name;
import net.libcsdbg.jtracer.annotation.constraint.NameConstraint;
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

	Process execute(@URI String workingDir, @Name String executable, Boolean async, String... args);

	List<Image> getProjectIcons();

	File getResource(@URI String path);

	ImageIcon loadIcon(@URI String name);

	Process mailTo(URL url);

	UtilityService passivate();
}
