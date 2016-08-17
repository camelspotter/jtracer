package net.libcsdbg.jtracer.service.persistence.jar;

import net.libcsdbg.jtracer.annotation.MixinNote;
import net.libcsdbg.jtracer.gui.component.ProgressBar;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.persistence.storage.FileSystemService;
import net.libcsdbg.jtracer.service.persistence.tools.Filter;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;

import java.io.*;
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
		public JarService extract(InputStream jarInputStream, File to, ProgressBar progressMonitor)
		{
			int offset = (progressMonitor == null) ? 0 : progressMonitor.getValue();

			try (DataOutputStream out = new DataOutputStream(new FileOutputStream(to))) {
				while (jarInputStream.available() > 0) {
					int data = jarInputStream.read();
					if (data == -1) {
						break;
					}

					out.writeByte(data);

					if (progressMonitor != null) {
						offset++;
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

		@Override
		public List<File> extractAll(File jar, File targetDirectory, ProgressBar progressMonitor, Class<? extends Filter> filterType)
		{
			List<File> retval = new LinkedList<>();

			if (targetDirectory == null) {
				targetDirectory = fileSystemSvc.createTemporaryDirectory(true);
			}

			/* The first entry in the result list is the directory the jar is extracted in */
			retval.add(targetDirectory);

			Map<String, Long> jarEntries = getEntryListing(jar);
			try (JarInputStream in = new JarInputStream(new FileInputStream(jar))) {
				Long offset = (progressMonitor != null) ? progressMonitor.getValue() : 0L;

				Filter filter = fileSystemSvc.createFileFilter(filterType);
				JarEntry entry;
				while ((entry = in.getNextJarEntry()) != null) {
					String name = entry.getName();
					File from = new File(name);
					File to = new File(targetDirectory, name);

					if (!filter.accept(from)) {
						if (progressMonitor == null || entry.isDirectory()) {
							continue;
						}

						offset += jarEntries.get(name);
						progressMonitor.stall(2);
						progressMonitor.setValue(offset.intValue());
						continue;
					}

					if (to.exists()) {
						continue;
					}

					if (entry.isDirectory()) {
						File subdir = targetDirectory;

						String separator = File.separator;
						if (separator.equals("\\")) {
							separator = separator.concat("\\");
						}

						for (String part : name.split(separator)) {
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

					extract(in, to, progressMonitor);
					if (progressMonitor != null) {
						progressMonitor.stall(50);
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
			try (JarFile file = new JarFile(jar)) {
				Map<String, Long> retval = new HashMap<>(Config.preallocSize);

				Enumeration<JarEntry> entries = file.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();

					Long size = (entry.isDirectory()) ? 0L : entry.getSize();

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
