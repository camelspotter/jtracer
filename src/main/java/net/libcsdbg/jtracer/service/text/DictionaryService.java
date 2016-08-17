package net.libcsdbg.jtracer.service.text;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.annotation.Note;
import net.libcsdbg.jtracer.annotation.constraint.Name;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.text.parse.Tokenizer;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Mixins(DictionaryService.Mixin.class)
@Activators(DictionaryService.Activator.class)
public interface DictionaryService extends DictionaryServiceApi,
                                           ServiceComposite
{
	public abstract class Mixin implements DictionaryService
	{
		@Structure
		protected Module selfContainer;

		@Service
		protected LoggerService loggerSvc;

		@Service
		protected UtilityService utilitySvc;


		@Override
		public DictionaryService activate()
		{
			if (active().get()) {
				return this;
			}

			words().set(new HashMap<>());

			loadDictionary("type");
			loadDictionary("keyword");
			loadDictionary("extension");

			metainfo().set("net.libcsdbg.jtracer");
			active().set(true);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' activated (" + metainfo().get() + ")");
			return this;
		}

		@Factory(Factory.Type.POJO)
		protected BufferedReader getDictionaryReader(@Name String name) throws FileNotFoundException
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

		@Factory(Factory.Type.COMPOSITE)
		@Override
		public Tokenizer getTokenizer(String grammar, String text)
		{
			TransientBuilder<Tokenizer> builder = selfContainer.newTransientBuilder(Tokenizer.class);

			builder.prototype()
			       .grammar()
			       .set(grammar);

			builder.prototype()
			       .input()
			       .set(text);

			return
				builder.newInstance()
				       .begin();
		}

		@Note("Using the same name will overwrite older dictionary data stored under that name")
		@Override
		public DictionaryService loadDictionary(String name)
		{
			try (BufferedReader reader = getDictionaryReader(name)) {
				List<String> words = new LinkedList<>();
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

				words().get()
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
				words().get()
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
		public DictionaryService passivate()
		{
			if (!active().get()) {
				return this;
			}

			words().get()
			       .values()
			       .forEach(List::clear);

			words().get()
			       .clear();

			words().set(null);
			active().set(false);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' passivated (" + metainfo().get() + ")");
			return this;
		}
	}


	public static class Activator extends ActivatorAdapter<ServiceReference<DictionaryService>>
	{
		@Override
		public void afterActivation(ServiceReference<DictionaryService> svc) throws Exception
		{
			svc.get()
			   .active()
			   .set(false);

			svc.get().activate();
		}

		@Override
		public void beforePassivation(ServiceReference<DictionaryService> svc) throws Exception
		{
			svc.get().passivate();
		}
	}
}
