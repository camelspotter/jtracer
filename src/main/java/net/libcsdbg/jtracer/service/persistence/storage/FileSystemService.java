package net.libcsdbg.jtracer.service.persistence.storage;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.annotation.MixinNote;
import net.libcsdbg.jtracer.gui.container.MainFrame;
import net.libcsdbg.jtracer.gui.component.ProgressBar;
import net.libcsdbg.jtracer.core.ApplicationCore;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.persistence.tools.Filter;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.composite.TransientBuilder;
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

		protected Byte[] box(byte[] buffer)
		{
			Byte[] retval = new Byte[buffer.length];
			for (int i = 0; i < buffer.length; i++) {
				retval[i] = buffer[i];
			}

			return retval;
		}

		@Override
		public FileSystemService copy(File from, File to, ProgressBar progressMonitor)
		{
			byte[] chunk = new byte[Config.chunkSize];

			int offset = (progressMonitor == null) ? 0 : progressMonitor.getValue();
			int size = (int) from.length();

			try (InputStream in = new FileInputStream(from);
			     DataOutputStream out = new DataOutputStream(new FileOutputStream(to))) {

				while (size > 0 && in.available() > 0) {
					int bytesToRead = (Config.chunkSize > size) ? size : Config.chunkSize;

					int bytesRead = in.read(chunk, 0, bytesToRead);
					if (bytesRead == -1) {
						break;
					}

					out.write(chunk, 0, bytesRead);
					size -= bytesRead;

					if (progressMonitor != null) {
						offset += bytesRead;
						progressMonitor.setValue(offset);
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
		public Filter createFileFilter(Class<? extends Filter> type)
		{
			TransientBuilder<? extends Filter> builder = selfContainer.newTransientBuilder(type);

			builder.prototype()
			       .type()
			       .set(type);

			return builder.newInstance();
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
					path = "." + registrySvc.getOrDefault("name", MainFrame.Config.name).trim();
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

		@Override
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

		@Override
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
			File home =
				FileSystemView.getFileSystemView()
				              .getHomeDirectory();

			if (home.getAbsolutePath().endsWith("Desktop")) {
				/* Invocation of home.getParent() may return null */
				return new File(home.getParent());
			}

			return home;
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
		public Byte[] read(File from)
		{
			int size = (int) from.length();
			int offset = 0;
			byte[] buffer = new byte[size];

			try (InputStream in = new FileInputStream(from)) {
				while (size > 0 && in.available() > 0) {
					int bytesRead = in.read(buffer, offset, size);
					if (bytesRead == -1) {
						break;
					}

					size -= bytesRead;
					offset += bytesRead;
				}

				return box(buffer);
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}

		@Override
		public FileSystemService save(File to, String content, Boolean append)
		{
			try (FileOutputStream out = new FileOutputStream(to, append)) {
				out.write(content.getBytes());
			}
			catch (RuntimeException err) {
				throw err;
			}
			catch (Throwable err) {
				throw new RuntimeException(err);
			}

			return this;
		}

		@Override
		public Boolean setExecutable(File f, Boolean executable, Boolean globally)
		{
			return f.setExecutable(executable, !globally);
		}

		@Override
		public Boolean setReadable(File f, Boolean readable, Boolean globally)
		{
			return f.setReadable(readable, !globally);
		}

		@Override
		public FileSystemService setResourcePrefix(String prefix)
		{
			dynamicPrefix = prefix;
			return this;
		}

		@Override
		public Boolean setWritable(File f, Boolean writable, Boolean globally)
		{
			return f.setWritable(writable, !globally);
		}


		public static class Config
		{
			public static Integer chunkSize = 128;
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
