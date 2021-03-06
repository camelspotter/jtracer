package net.libcsdbg.jtracer.service.parser;

import net.libcsdbg.jtracer.annotation.Mutable;
import net.libcsdbg.jtracer.annotation.Note;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.utility.UtilityService;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mixins(ParserService.Mixin.class)
@Activators(ParserService.Activator.class)
public interface ParserService extends ParserServiceApi, ServiceComposite
{
	@Mutable
	public abstract class Mixin implements ParserService
	{
		@Service
		protected LoggerService loggerSvc;

		@Service
		protected UtilityService utilitySvc;


		@Override
		public ParserService activate()
		{
			if (active().get()) {
				return this;
			}

			state().set(new HashMap<>());

			loadDictionary("type");
			loadDictionary("keyword");
			loadDictionary("extension");

			metainfo().set("net.libcsdbg.jtracer");
			active().set(true);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' activated (" + metainfo().get() + ")");
			return this;
		}

		protected BufferedReader getDictionaryReader(String name) throws FileNotFoundException
		{
			File src = utilitySvc.getResource("dictionary/" + name + ".dict");

			if (!src.exists()) {
				throw new RuntimeException("Dictionary '" + src.getAbsolutePath() + "' doesn't exist");
			}

			if (!src.isFile()) {
				throw new RuntimeException("Path '" + src.getAbsolutePath() + "' is not a dictionary file");
			}

			if (!src.canRead()) {
				throw new RuntimeException("Dictionary '" + src.getAbsolutePath() + "' is not readable");
			}

			return new BufferedReader(new FileReader(src));
		}

		@Note("Using the same name will overwrite older dictionary data stored under that name")
		@Override
		public ParserService loadDictionary(String name)
		{
			try {
				BufferedReader reader = getDictionaryReader(name);

				List<String> words = new ArrayList<>();
				while (true) {
					String line = reader.readLine();
					if (line == null) {
						break;
					}

					line = line.trim();
					if (line.length() > 0) {
						words.add(line);
					}
				}

				reader.close();
				state().get()
				       .put(name, words);

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
		public Boolean lookup(String token, String dictionary, Boolean regex)
		{
			List<String> words =
				state().get()
				       .get(dictionary);

			if (words == null) {
				return false;
			}

			for (String word : words) {
				if (regex) {
					if (token.matches(word)) {
						return true;
					}
				}
				else if (token.equals(word)) {
					return true;
				}
			}

			return false;
		}

		@Override
		public ParserService passivate()
		{
			if (!active().get()) {
				return this;
			}

			state().get()
			       .values()
			       .forEach(List::clear);

			state().get()
			       .clear();

			state().set(null);
			active().set(false);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' passivated (" + metainfo().get() + ")");
			return this;
		}
	}


	@Mutable(false)
	class Activator extends ActivatorAdapter<ServiceReference<ParserService>>
	{
		@Override
		public void afterActivation(ServiceReference<ParserService> svc) throws Exception
		{
			svc.get()
			   .active()
			   .set(false);

			svc.get().activate();
		}

		@Override
		public void beforePassivation(ServiceReference<ParserService> svc) throws Exception
		{
			svc.get().passivate();
		}
	}
}
