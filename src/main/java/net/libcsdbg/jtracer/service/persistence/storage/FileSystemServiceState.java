package net.libcsdbg.jtracer.service.persistence.storage;

import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

public interface FileSystemServiceState
{
	Property<Boolean> active();

	@NotEmpty
	Property<String> metainfo();
}
