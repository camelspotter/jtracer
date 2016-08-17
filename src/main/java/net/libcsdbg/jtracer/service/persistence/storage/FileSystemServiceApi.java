package net.libcsdbg.jtracer.service.persistence.storage;

import net.libcsdbg.jtracer.gui.component.ProgressBar;
import net.libcsdbg.jtracer.service.persistence.tools.Filter;
import org.qi4j.api.common.Optional;

import java.io.File;

public interface FileSystemServiceApi extends FileSystemServiceState
{
	FileSystemService activate();

	FileSystemService copy(File from, File to, @Optional ProgressBar progressMonitor);

	Filter createFileFilter(@Optional Class<? extends Filter> type);

	File createTemporaryDirectory(Boolean autoDelete, String... components);

	FileSystemService deleteRecursively(File file);

	FileSystemService deleteSingle(File file);

	File getHomeDirectory();

	File getResourcePrefix();

	Boolean isAccessibleDirectory(File dir);

	FileSystemService passivate();

	Byte[] read(File from);

	FileSystemService save(File to, String content, Boolean append);

	Boolean setExecutable(File f, Boolean executable, Boolean globally);

	Boolean setReadable(File f, Boolean readable, Boolean globally);

	FileSystemService setResourcePrefix(String prefix);

	Boolean setWritable(File f, Boolean writable, Boolean globally);
}
