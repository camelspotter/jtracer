package net.libcsdbg.jtracer.service.persistence.jar;

import net.libcsdbg.jtracer.component.ProgressBar;
import org.qi4j.api.common.Optional;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface JarServiceApi extends JarServiceState
{
	JarService activate();

	File extract(@Optional File jar, @Optional File dir, String entry, @Optional ProgressBar progressMonitor);

	List<File> extractAll(@Optional File jar, @Optional File dir, @Optional ProgressBar progressMonitor);

	Map<String, Long> getEntryListing(@Optional File jar);

	Long getTotalUncompressedSize(@Optional File jar);

	JarService passivate();

	File resolveJar(@Optional File jar);

	File resolveTargetDirectory(@Optional File dir, @Optional Boolean autoDelete);
}
