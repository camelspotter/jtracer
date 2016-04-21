package net.libcsdbg.jtracer.service.text.parse;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.annotation.MixinNote;
import net.libcsdbg.jtracer.service.text.DictionaryService;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.library.constraints.annotation.NotEmpty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.libcsdbg.jtracer.service.text.parse.Token.Type;

@Mixins(Tokenizer.Mixin.class)
public interface Tokenizer extends TokenizerState,
                                   TransientComposite
{
	Tokenizer begin();

	Boolean hasRemainder();

	Token next();

	Tokenizer reload(@NotEmpty String input);

	Tokenizer reload(@NotEmpty String grammar, @NotEmpty String input);

	Token remainder();

	Tokenizer reset();

	Type resolveTokenType(@NotEmpty String tokenText, @Optional String delimiter);

	Token tokenOf(@NotEmpty String text, @Optional String delimiter, Type type);


	@MixinNote("The default implementation is based on java.util.regex and it is C++ specific")
	public abstract class Mixin implements Tokenizer
	{
		@Structure
		protected Module selfContainer;

		@Service
		protected DictionaryService dictionarySvc;


		protected Pattern pattern;

		protected Matcher matcher;

		protected Integer offset;


		@Override
		public Tokenizer begin()
		{
			offset = 0;

			pattern = Pattern.compile(grammar().get());
			matcher = pattern.matcher(input().get());

			return this;
		}

		@Override
		public Boolean hasRemainder()
		{
			return offset < input().get().length();
		}

		@Override
		public Token next()
		{
			if (!matcher.find()) {
				return null;
			}

			String delimiter = matcher.group();
			String tokenText = input().get().substring(offset, matcher.start());
			offset = matcher.end();

			return tokenOf(tokenText, delimiter, resolveTokenType(tokenText, delimiter));
		}

		@Override
		public Tokenizer reload(String input)
		{
			input().set(input);

			offset = 0;
			matcher = pattern.matcher(input);

			return this;
		}

		@Override
		public Tokenizer reload(String grammar, String input)
		{
			grammar().set(grammar);
			input().set(input);

			return begin();
		}

		@Override
		public Token remainder()
		{
			if (!hasRemainder()) {
				return null;
			}

			String tokenText = input().get().substring(offset);
			return tokenOf(tokenText, null, resolveTokenType(tokenText, null));
		}

		@Override
		public Tokenizer reset()
		{
			offset = 0;
			matcher.reset();
			return this;
		}

		@Override
		public Type resolveTokenType(String tokenText, String delimiter)
		{
			/* Decimal and hex numbers */
			if (tokenText.matches(Config.numberPattern)) {
				return Type.number;
			}

			/* File names */
			else if (dictionarySvc.lookup(tokenText, "extension", true)) {
				return Type.file;
			}

			/* C++ integral types */
			else if (dictionarySvc.lookup(tokenText, "type", false)) {
				return Type.type;
			}

			/* C++ keywords (apart those for integral types) */
			else if (dictionarySvc.lookup(tokenText, "keyword", false)) {
				return Type.keyword;
			}

			else if (delimiter != null) {
				/* C++ namespaces and classes */
				if (delimiter.equals("::")) {
					return Type.scope;
				}

				/* Function names */
				else if (delimiter.equals("(") ||
				         delimiter.equals("<") ||
				         delimiter.startsWith("\n")) {
					return Type.function;
				}
			}

			return Type.plain;
		}

		@Factory
		@Override
		public Token tokenOf(String text, String delimiter, Type type)
		{
			ValueBuilder<Token> builder = selfContainer.newValueBuilder(Token.class);

			builder.prototype()
			       .text()
			       .set(text);

			builder.prototype()
			       .delimiter()
			       .set(delimiter);

			builder.prototype()
			       .type()
			       .set(type);

			return builder.newInstance();
		}


		public static class Config
		{
			public static String grammar = "[\\s\\{\\}\\(\\)\\*&,:<>]+";

			public static String numberPattern = "(0x)?\\p{XDigit}+$";
		}
	}
}
