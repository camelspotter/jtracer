package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.text.DictionaryService;
import net.libcsdbg.jtracer.service.text.parse.Token;
import net.libcsdbg.jtracer.service.text.parse.Tokenizer;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TracePane extends JTextPane implements AutoInjectable
{
	private static final long serialVersionUID = -1330785540803041156L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected DictionaryService dictionarySvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;


	protected Map<String, String> request;


	public TracePane()
	{
		super();
		selfInject();
		request = new HashMap<>();
	}

	public TracePane(Map<String, String> request)
	{
		super();
		selfInject();
		this.request = request;

		/* Create the default style */
		Style style =
			addStyle("plain",
			         StyleContext.getDefaultStyleContext()
			                     .getStyle(StyleContext.DEFAULT_STYLE));

		/* Create paragraph style attributes */
		String param = registrySvc.get("trace-padding");
		if (param != null) {
			Integer padding = Integer.parseInt(param.trim());
			StyleConstants.setLeftIndent(style, padding);
			StyleConstants.setRightIndent(style, padding);
		}

		param = registrySvc.get("trace-line-height");
		if (param != null) {
			StyleConstants.setLineSpacing(style, Float.parseFloat(param.trim()));
		}

		StyleConstants.setAlignment(style, StyleConstants.ALIGN_JUSTIFIED);
		setParagraphAttributes(style, true);

		/* Setup the default (plain) style */
		Font font = componentSvc.getFont("trace");
		StyleConstants.setFontFamily(style, font.getFamily());
		StyleConstants.setFontSize(style, font.getSize());
		StyleConstants.setBold(style, font.isBold());
		StyleConstants.setItalic(style, font.isItalic());

		StyleConstants.setForeground(style, componentSvc.getForegroundColor("trace-plain"));

		/* Based on the default style create a style for each type of token */
		for (Token.Type type : Token.Type.values()) {
			String name = type.name();
			if (name.equals("plain")) {
				continue;
			}

			style = addStyle(name, style);
			StyleConstants.setForeground(style, componentSvc.getForegroundColor("trace-" + name));
		}

		/* Set selection colors */
		setSelectionColor(componentSvc.getBackgroundColor("trace-selection"));
		setSelectedTextColor(componentSvc.getForegroundColor("trace-selection"));

		setMargin(componentSvc.getInsets("trace"));
		setEditable(false);
		setAutoscrolls(true);
	}

	public TracePane append(String trace)
	{
		Tokenizer tokenizer = dictionarySvc.getTokenizer(Tokenizer.Mixin.Config.grammar, trace);

		Token token;
		while ((token = tokenizer.next()) != null) {
			append(token);
		}

		if (tokenizer.hasRemainder()) {
			append(tokenizer.remainder());
		}

		return this;
	}

	public TracePane append(Token token)
	{
		append(token.text().get(),
		       token.type()
		            .get()
		            .name());

		String delimiter = token.delimiter().get();
		if (delimiter != null) {
			return append(delimiter, "delimiter");
		}

		return this;
	}

	public TracePane append(String text, String tag)
	{
		try {
			Document doc = getDocument();
			int length = doc.getLength();

			doc.insertString(length, text, getStyle(tag));
			setCaretPosition(length + text.length());
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}

		return this;
	}

	public String getRequestSection(String section)
	{
		return request.get(section);
	}
}
