package net.libcsdbg.jtracer.service.text;

import net.libcsdbg.jtracer.annotation.constraint.Name;
import net.libcsdbg.jtracer.annotation.constraint.NameConstraint;
import net.libcsdbg.jtracer.service.text.parse.Tokenizer;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.library.constraints.annotation.NotEmpty;

@Constraints(NameConstraint.class)
public interface DictionaryServiceApi extends DictionaryServiceState
{
	DictionaryService activate();

	Tokenizer getTokenizer(@NotEmpty String grammar, @NotEmpty String text);

	DictionaryService loadDictionary(@Name String name);

	Boolean lookup(@NotEmpty String token, @Name String dictionary, Boolean regex);

	DictionaryService passivate();
}
