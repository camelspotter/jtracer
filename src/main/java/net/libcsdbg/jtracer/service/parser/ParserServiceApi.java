package net.libcsdbg.jtracer.service.parser;

/* todo Add API to generate a parser composite */
public interface ParserServiceApi extends ParserServiceState
{
	ParserService activate();

	ParserService loadDictionary(String name);

	Boolean lookup(String token, String dictionary, Boolean regex);

	ParserService passivate();
}
