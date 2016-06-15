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

	FileSystemService deleteRecursively(File file);

	FileSystemService deleteSingle(File file);

	File getHomeDirectory();

	File getResourcePrefix();

	Boolean isAccessibleDirectory(File dir);

	FileSystemService passivate();

	FileSystemService setResourcePrefix(String prefix);
}
