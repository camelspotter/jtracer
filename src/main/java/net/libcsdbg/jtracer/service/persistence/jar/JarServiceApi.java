package net.libcsdbg.jtracer.service.persistence.jar;

import net.libcsdbg.jtracer.gui.component.ProgressBar;
import net.libcsdbg.jtracer.service.persistence.tools.Filter;
import org.qi4j.api.common.Optional;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface JarServiceApi extends JarServiceState
{
	JarService activate();

	JarService extract(InputStream jarInputStream, File to, @Optional ProgressBar progressMonitor);

	List<File> extractAll(File jar, @Optional File targetDirectory, @Optional ProgressBar progressMonitor, @Optional Class<? extends Filter> filterType);

	Map<String, Long> getEntryListing(File jar);

	Long getTotalUncompressedSize(File jar);

	JarService passivate();
}
