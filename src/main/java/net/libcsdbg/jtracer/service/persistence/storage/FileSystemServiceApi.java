package net.libcsdbg.jtracer.service.persistence.storage;

import net.libcsdbg.jtracer.component.ProgressBar;
import net.libcsdbg.jtracer.service.persistence.tools.FileFilter;
import org.qi4j.api.common.Optional;

import java.io.File;

public interface FileSystemServiceApi extends FileSystemServiceState
{
	FileSystemService activate();

	FileSystemService copy(File src, File dst, @Optional ProgressBar progressMonitor);

	FileFilter createFileFilter();

	File createTemporaryDirectory(Boolean autoDelete, String... components);

	File getHomeDirectory();

	File getResourcePrefix();

	FileSystemService passivate();

	FileSystemService setResourcePrefix(String prefix);
}
