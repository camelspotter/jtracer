package net.libcsdbg.jtracer.service.text.parser;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.annotation.MixinNote;
import net.libcsdbg.jtracer.service.text.ParserService;
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

import static net.libcsdbg.jtracer.service.text.parser.Token.Type;

@Mixins(Tokenizer.Mixin.class)
public interface Tokenizer extends TokenizerState, TransientComposite
{
	Tokenizer begin();

	Token getToken(@NotEmpty String text, @Optional String delimiter, Type type);

	Boolean hasRemainder();

	Token next();

	Tokenizer reload(@NotEmpty String input);

	Tokenizer reload(@NotEmpty String grammar, @NotEmpty String input);

	Token remainder();

	Tokenizer reset();

	Type resolveTokenType(@NotEmpty String tokenText, @Optional String delimiter);


	@MixinNote("The default implementation is based on java.util.regex and it is C++ trace specific")
	public abstract class Mixin implements Tokenizer
	{
		@Structure
		protected Module selfContainer;

		@Service
		protected ParserService parserSvc;


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

		@Factory
		@Override
		public Token getToken(String text, String delimiter, Type type)
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

			return getToken(tokenText, delimiter, resolveTokenType(tokenText, delimiter));
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
			return getToken(tokenText, null, resolveTokenType(tokenText, null));
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
			String num = "(0x)?\\p{XDigit}+$";

			/* Decimal and hex numbers */
			if (tokenText.matches(num)) {
				return Type.number;
			}

			/* File names */
			else if (parserSvc.lookup(tokenText, "extension", true)) {
				return Type.file;
			}

			/* C++ integral types */
			else if (parserSvc.lookup(tokenText, "type", false)) {
				return Type.type;
			}

			/* C++ keywords (apart those for integral types) */
			else if (parserSvc.lookup(tokenText, "keyword", false)) {
				return Type.keyword;
			}

			else if (delimiter != null) {
				/* C++ namespaces and classes */
				if (delimiter.equals("::")) {
					return Type.scope;
				}

				/* Function names */
				else if (delimiter.equals("(") || delimiter.equals("<") || delimiter.startsWith("\n")) {
					return Type.function;
				}
			}

			return Type.plain;
		}
	}
}
