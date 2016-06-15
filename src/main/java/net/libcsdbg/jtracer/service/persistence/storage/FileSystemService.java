package net.libcsdbg.jtracer.service.persistence.storage;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.annotation.MixinNote;
import net.libcsdbg.jtracer.component.MainFrame;
import net.libcsdbg.jtracer.component.ProgressBar;
import net.libcsdbg.jtracer.core.ApplicationCore;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.persistence.tools.FileFilter;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.Files;

@Mixins(FileSystemService.Mixin.class)
@Activators(FileSystemService.Activator.class)
public interface FileSystemService extends FileSystemServiceApi,
                                           ServiceComposite
{
	@MixinNote("The default service implementation is based on java.io and java.nio")
	public abstract class Mixin implements FileSystemService
	{
		protected static String dynamicPrefix = null;


		@Structure
		protected Module selfContainer;

		@Service
		protected LoggerService loggerSvc;

		@Service
		protected RegistryService registrySvc;


		@Override
		public FileSystemService activate()
		{
			if (active().get()) {
				return this;
			}

			metainfo().set("java.io + java.nio");
			active().set(true);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' activated (" + metainfo().get() + ")");
			return this;
		}

		@Override
		public FileSystemService copy(File src, File dst, @Optional ProgressBar progressMonitor)
		{
			int start = (progressMonitor == null) ? 0 : progressMonitor.getValue();

			try (InputStream in = new FileInputStream(src);
			     DataOutputStream out = new DataOutputStream(new FileOutputStream(dst))) {
				while (in.available() > 0) {
					int data = in.read();
					if (data == -1) {
						break;
					}

					out.writeByte(data);

					if (progressMonitor != null) {
						start++;

						if (start % 10 == 0) {
							progressMonitor.setValue(start);
						}
					}
				}

				return this;
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}

		@Factory(Factory.Type.COMPOSITE)
		@Override
		public FileFilter createFileFilter()
		{
			return selfContainer.newTransient(FileFilter.class);
		}

		@Override
		public File createTemporaryDirectory(Boolean autoDelete, String... components)
		{
			try {
				String path;
				if (components.length > 0) {
					path = String.join("_", components);
				}

				else {
					path = registrySvc.get("name");
					if (path == null) {
						path = MainFrame.Config.name;
					}

					path = "." + path.trim();
				}

				File retval =
					Files.createTempDirectory(path)
					     .toFile();

				if (autoDelete) {
					retval.deleteOnExit();
				}

				return retval;
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}

		public FileSystemService deleteRecursively(File file)
		{
			if (!file.isDirectory()) {
				return deleteSingle(file);
			}

			File[] listing = file.listFiles();
			if (listing == null || listing.length == 0) {
				return deleteSingle(file);
			}

			for (File f : listing) {
				if (f.isDirectory()) {
					deleteRecursively(f);
				}
				else {
					deleteSingle(f);
				}
			}

			return deleteSingle(file);
		}

		public FileSystemService deleteSingle(File file)
		{
			if (!file.exists()) {
				return this;
			}

			else if (!file.delete()) {
				file.deleteOnExit();
			}

			return this;
		}

		@Override
		public File getHomeDirectory()
		{
			return
				FileSystemView.getFileSystemView()
				              .getHomeDirectory();
		}

		@Override
		public File getResourcePrefix()
		{
			if (dynamicPrefix != null) {
				return new File(dynamicPrefix);
			}
			else if (ApplicationCore.getInstaller()
			                        .isSelfExecutableJar()) {
				dynamicPrefix = System.getProperty("java.class.path");
			}

			try {
				File retval =
					new File(getClass().getProtectionDomain()
					                   .getCodeSource()
					                   .getLocation()
					                   .toURI());

				if (!retval.isDirectory()) {
					retval = retval.getParentFile();
				}

				dynamicPrefix = retval.getCanonicalPath();
				return retval;
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}

		@Override
		public Boolean isAccessibleDirectory(File dir)
		{
			return
				dir.exists() &&
				dir.canRead() &&
				dir.canExecute() &&
				dir.isDirectory();
		}

		@Override
		public FileSystemService passivate()
		{
			if (!active().get()) {
				return this;
			}

			active().set(false);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' passivated (" + metainfo().get() + ")");
			return this;
		}

		@Override
		public FileSystemService setResourcePrefix(String prefix)
		{
			dynamicPrefix = prefix;
			return this;
		}
	}


	public static class Activator extends ActivatorAdapter<ServiceReference<FileSystemService>>
	{
		@Override
		public void afterActivation(ServiceReference<FileSystemService> svc) throws Exception
		{
			svc.get()
			   .active()
			   .set(false);

			svc.get().activate();
		}

		@Override
		public void beforePassivation(ServiceReference<FileSystemService> svc) throws Exception
		{
			svc.get().passivate();
		}
	}
}
