package net.libcsdbg.jtracer.service.persistence.jar;

import net.libcsdbg.jtracer.annotation.MixinNote;
import net.libcsdbg.jtracer.component.MainFrame;
import net.libcsdbg.jtracer.component.ProgressBar;
import net.libcsdbg.jtracer.core.ApplicationCore;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.persistence.storage.FileSystemService;
import net.libcsdbg.jtracer.service.persistence.tools.FileFilter;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;

import javax.swing.filechooser.FileSystemView;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

@Mixins(JarService.Mixin.class)
@Activators(JarService.Activator.class)
public interface JarService extends JarServiceApi,
                                    ServiceComposite
{
	@MixinNote("The default service implementation is based on java.util.jar")
	public abstract class Mixin implements JarService
	{
		@Service
		protected FileSystemService fileSystemSvc;

		@Service
		protected LoggerService loggerSvc;


		@Override
		public JarService activate()
		{
			if (active().get()) {
				return this;
			}

			metainfo().set("java.util.jar");
			active().set(true);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' activated (" + metainfo().get() + ")");
			return this;
		}

		@Override
		public File extract(File jar, File dir, String entry, ProgressBar progressMonitor)
		{
			return null;
		}

		@Override
		public List<File> extractAll(File jar, File dir, ProgressBar progressMonitor)
		{
			List<File> retval = new ArrayList<>(Config.preallocSize);

			jar = resolveJar(jar);
			dir = resolveTargetDirectory(dir, null);
			resetResourcePrefix(jar, dir);

			/* The first entry in the result list is the directory the jar is extracted in */
			retval.add(dir);

			Map<String, Long> jarEntries = getEntryListing(jar);
			try (JarInputStream in = new JarInputStream(new FileInputStream(jar))) {
				Long bytes = (progressMonitor != null) ? progressMonitor.getValue() : 0L;

				FileFilter filter = fileSystemSvc.createFileFilter();
				JarEntry entry;
				while ((entry = in.getNextJarEntry()) != null) {
					File from = new File(entry.getName());
					File to = new File(dir, entry.getName());

					if (!filter.accept(from)) {
						if (progressMonitor == null || entry.isDirectory()) {
							continue;
						}

						bytes += jarEntries.get(entry.getName());
						/* progressMonitor.delay(); */
						progressMonitor.setValue(bytes.intValue());
						continue;
					}

					if (to.exists()) {
						continue;
					}

					if (entry.isDirectory()) {
						File subdir = dir;

						String separator = File.separator;
						if (separator.equals("\\")) {
							separator = separator.concat("\\");
						}

						for (String part : entry.getName().split(separator)) {
							subdir = new File(subdir, part);

							if (subdir.exists()) {
								continue;
							}

							if (!subdir.mkdir()) {
								throw new RuntimeException("Failed to create sub-directory '" + subdir.getAbsolutePath() + "'");
							}

							retval.add(subdir);
						}

						continue;
					}

					retval.add(to);
					if (!to.createNewFile()) {
						throw new RuntimeException("Failed to create file '" + to.getAbsolutePath() + "'");
					}

					DataOutputStream out = new DataOutputStream(new FileOutputStream(to));
					while (in.available() >= 1) {
						int data = in.read();
						if (data == -1) {
							break;
						}

						out.writeByte(data);

						if (progressMonitor != null) {
							bytes++;
							progressMonitor.setValue(bytes.intValue());
						}
					}

					out.close();
					if (progressMonitor != null) {
						progressMonitor.delay(50L);
					}
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
		public Map<String, Long> getEntryListing(File jar)
		{
			jar = resolveJar(jar);

			try (JarFile file = new JarFile(jar)) {
				Map<String, Long> retval = new HashMap<>(Config.preallocSize);

				Enumeration<JarEntry> entries = file.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();

					Long size = (entry.isDirectory()) ? 0 : entry.getSize();

					String name = entry.getName();
					if (retval.containsKey(name)) {
						size += retval.get(name);
					}

					retval.put(name, size);
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
		public Long getTotalUncompressedSize(File jar)
		{
			jar = resolveJar(jar);
			Long retval = 0L;

			try (JarFile file = new JarFile(jar)) {
				Enumeration<JarEntry> entries = file.entries();

				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();

					if (!entry.isDirectory()) {
						retval += entry.getSize();
					}
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
		public JarService passivate()
		{
			if (!active().get()) {
				return this;
			}

			active().set(false);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' passivated (" + metainfo().get() + ")");
			return this;
		}

		protected JarService resetResourcePrefix(File jar, File dir)
		{
			boolean selfexec =
				ApplicationCore.getInstaller()
				               .isSelfExecutableJar();

			if (selfexec) {
				fileSystemSvc.setResourcePrefix(dir.getAbsolutePath());
			}

			StringBuilder message = new StringBuilder(Config.preallocSize);
			message.append("Java archive '")
			       .append(jar.getAbsolutePath())
			       .append("' ");

			if (selfexec) {
				message.append("(self-executable and self-extracted) ");
			}

			message.append("extracted in '")
			       .append(dir.getAbsolutePath())
			       .append("'");

			loggerSvc.info(getClass(), message.toString());
			return this;
		}

		@Override
		public File resolveJar(File jar)
		{
			if (jar != null) {
				return jar;
			}

			String classpath = System.getProperty("java.class.path");
			jar = new File(classpath);

			if (!ApplicationCore.getInstaller().isSelfExecutableJar() ||
			    !jar.exists() ||
			    !jar.canRead()) {
				throw new RuntimeException("Failed to auto-detect a self-extracted jar file (classpath -> " + classpath + ")");
			}

			return jar;
		}

		@Override
		public File resolveTargetDirectory(@Optional File dir, @Optional Boolean autoDelete)
		{
			if (dir != null) {
				return dir;
			}

			if (ApplicationCore.getInstaller().isInstalled() || autoDelete == null) {
				if (autoDelete == null) {
					autoDelete = true;
				}

				return fileSystemSvc.createTemporaryDirectory(autoDelete);
			}

			String path =
				FileSystemView.getFileSystemView()
				              .getHomeDirectory()
				              .getAbsolutePath();

			path =
				path.concat(File.separator)
				    .concat(".")
				    .concat(MainFrame.Config.name)
				    .concat(File.separator);

			File retval = new File(path);
			if (!retval.mkdirs()) {
				throw new RuntimeException("Failed to create the installation prefix '" + retval.getAbsolutePath() + "'");
			}

			return retval;
		}


		public static class Config
		{
			public static Integer preallocSize = 128;
		}
	}


	public static class Activator extends ActivatorAdapter<ServiceReference<JarService>>
	{
		@Override
		public void afterActivation(ServiceReference<JarService> svc) throws Exception
		{
			svc.get()
			   .active()
			   .set(false);

			svc.get().activate();
		}

		@Override
		public void beforePassivation(ServiceReference<JarService> svc) throws Exception
		{
			svc.get().passivate();
		}
	}
}
