package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.component.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.parser.ParserService;
import net.libcsdbg.jtracer.service.registry.RegistryService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TracePane extends JTextPane implements AutoInjectable
{
	private static final long serialVersionUID = -1330785540803041156L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;

	@Service
	protected ParserService parserSvc;


	protected Map<String, String> details;


	public TracePane()
	{
		super();
		selfInject();
		details = new HashMap<>();
	}

	public TracePane(Map<String, String> request)
	{
		super();
		selfInject();
		details = request;

		Style style = addStyle("plain", StyleContext.getDefaultStyleContext()
		                                        .getStyle(StyleContext.DEFAULT_STYLE));

		/* Create paragraph style attributes */
		String param = registrySvc.get("trace-padding");
		if (param != null) {
			Integer padding = Integer.parseInt(param);
			StyleConstants.setLeftIndent(style, padding);
			StyleConstants.setRightIndent(style, padding);
		}

		param = registrySvc.get("trace-line-height");
		if (param != null) {
			StyleConstants.setLineSpacing(style, Float.parseFloat(param));
		}

		StyleConstants.setAlignment(style, StyleConstants.ALIGN_JUSTIFIED);
		setParagraphAttributes(style, true);

		/* Setup the default (plain) style */
		Font font = componentSvc.getFont("trace");
		if (font != null) {
			StyleConstants.setFontFamily(style, font.getFamily());
			StyleConstants.setFontSize(style, font.getSize());
			StyleConstants.setBold(style, font.isBold());
			StyleConstants.setItalic(style, font.isItalic());
		}

		StyleConstants.setForeground(style, componentSvc.getForegroundColor("trace-plain"));

		/* Based on the default style create a style for each type of token */
		style = addStyle("keyword", style);
		StyleConstants.setForeground(style, componentSvc.getForegroundColor("trace-keyword"));

		style = addStyle("type", style);
		StyleConstants.setForeground(style, componentSvc.getForegroundColor("trace-type"));

		style = addStyle("number", style);
		StyleConstants.setForeground(style, componentSvc.getForegroundColor("trace-number"));

		style = addStyle("file", style);
		StyleConstants.setForeground(style, componentSvc.getForegroundColor("trace-file"));

		style = addStyle("delimiter", style);
		StyleConstants.setForeground(style, componentSvc.getForegroundColor("trace-delimiter"));

		style = addStyle("scope", style);
		StyleConstants.setForeground(style, componentSvc.getForegroundColor("trace-scope"));

		style = addStyle("function", style);
		StyleConstants.setForeground(style, componentSvc.getForegroundColor("trace-function"));

		/* Set selection colors */
		setSelectionColor(componentSvc.getBackgroundColor("trace-selection"));
		setSelectedTextColor(componentSvc.getForegroundColor("trace-selection"));

		setMargin(componentSvc.getInsets("trace"));
		setEditable(false);
		setAutoscrolls(true);
	}

	public TracePane append(String trace)
	{
		/* Delimiters */
		String expr = "[\\s\\{\\}\\(\\)\\*&,:<>]+";

		Pattern regexp = Pattern.compile(expr);
		Matcher parser = regexp.matcher(trace);

		/* Parse the trace and append it word-by-word doing syntax highlighting */
		int offset = 0;
		String prev = null;
		while (parser.find()) {
			String grp = parser.group();
			String token = trace.substring(offset, parser.start());
			offset = parser.end();

			append(token, prev, grp);
			append(grp, "delimiter");
			prev = grp;
		}

		if (offset < trace.length() - 1) {
			append(trace.substring(offset), prev, null);
		}

		return this;
	}

	public TracePane append(String token, String prev, String next)
	{
		String num = "(0x)?\\p{XDigit}+$";

		/* Highlight decimal and hex numbers */
		if (token.matches(num)) {
			append(token, "number");
		}

		/* Highlight file names */
		else if (parserSvc.lookup(token, "extension", true)) {
			append(token, "file");
		}

		/* Highlight C++ integral types */
		else if (parserSvc.lookup(token, "type", false)) {
			append(token, "type");
		}

		/* Highlight C++ keywords (apart those for integral types) */
		else if (parserSvc.lookup(token, "keyword", false)) {
			append(token, "keyword");
		}

		/* Highlight C++ namespaces and classes */
		else if (next.equals("::")) {
			append(token, "scope");
		}

		/* Highlight function names */
		else if (next.equals("(") || next.equals("<") || next.startsWith("\n")) {
			append(token, "function");
		}

		else {
			append(token, "plain");
		}

		return this;
	}

	public TracePane append(String line, String tag)
	{
		try {
			Document doc = getDocument();
			int length = doc.getLength();

			doc.insertString(length, line, getStyle(tag));
			setCaretPosition(length + line.length());
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}

		return this;
	}

	public TracePane appendln(String line, String tag)
	{
		return append(line + "\n", tag);
	}

	public TracePane clear()
	{
		try {
			Document doc = getDocument();
			doc.remove(0, doc.getLength());
			setCaretPosition(0);
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}

		return this;
	}

	public String getField(String key)
	{
		return details.get(key);
	}

	public Set<String> getFieldKeys()
	{
		return details.keySet();
	}
}
