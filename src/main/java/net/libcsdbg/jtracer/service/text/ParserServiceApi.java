package net.libcsdbg.jtracer.service.text;

import net.libcsdbg.jtracer.service.text.parser.Tokenizer;

public interface ParserServiceApi extends ParserServiceState
{
	ParserService activate();

	Tokenizer getTokenizer(String grammar, String text);

	ParserService loadDictionary(String name);

	Boolean lookup(String token, String dictionary, Boolean regex);

	ParserService passivate();
}
