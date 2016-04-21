package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;

/* todo Finalize look and feel, colors */

public class LogPane extends JTextPane implements AutoInjectable
{
	private static final long serialVersionUID = 7733740725310300281L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;


	public LogPane()
	{
		super();
		selfInject();

		Style style =
			addStyle("debug",
			         StyleContext.getDefaultStyleContext()
			                     .getStyle(StyleContext.DEFAULT_STYLE));

		/* Create paragraph style attributes */
		String param = registrySvc.get("log-padding");
		if (param != null) {
			Integer padding = Integer.parseInt(param);
			StyleConstants.setLeftIndent(style, padding);
			StyleConstants.setRightIndent(style, padding);
		}

		param = registrySvc.get("log-line-height");
		if (param != null) {
			StyleConstants.setLineSpacing(style, Float.parseFloat(param));
		}

		StyleConstants.setAlignment(style, StyleConstants.ALIGN_JUSTIFIED);
		setParagraphAttributes(style, true);

		/* Setup the default (debug) style */
		Font font = componentSvc.getFont("log");
		if (font != null) {
			StyleConstants.setFontFamily(style, font.getFamily());
			StyleConstants.setFontSize(style, font.getSize());
			StyleConstants.setBold(style, font.isBold());
			StyleConstants.setItalic(style, font.isItalic());
		}

		StyleConstants.setForeground(style, componentSvc.getForegroundColor("log-debug"));

		/* Based on the default style create a style for each type of message */
		style = addStyle("status", style);
		StyleConstants.setForeground(style, componentSvc.getForegroundColor("log-status"));

		style = addStyle("data", style);
		StyleConstants.setForeground(style, componentSvc.getForegroundColor("log-data"));

		style = addStyle("error", style);
		StyleConstants.setForeground(style, componentSvc.getForegroundColor("log-error"));

		style = addStyle("alert", style);
		StyleConstants.setForeground(style, componentSvc.getForegroundColor("log-alert"));

		/* Set selection colors */
		setSelectionColor(componentSvc.getBackgroundColor("log-selection"));
		setSelectedTextColor(componentSvc.getForegroundColor("log-selection"));

		setMargin(componentSvc.getInsets("log"));
		setEditable(false);
		setAutoscrolls(false);
	}

	public LogPane append(final String line, final String tag, Boolean... sync)
	{
		/* If the call is within an event dispatching thread */
		if (SwingUtilities.isEventDispatchThread()) {
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

		/* Register a Runnable to be called by the event dispatching thread */
		try {
			Runnable task = () -> append(line, tag);

			if (sync.length > 0 && sync[0]) {
				SwingUtilities.invokeAndWait(task);
			}
			else {
				SwingUtilities.invokeLater(task);
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

	public LogPane appendln(String line, String tag, Boolean... sync)
	{
		append(tag + "> ", "debug", sync);
		return append(line + "\n", tag, sync);
	}

	public LogPane clear()
	{
		try {
			Document doc = getDocument();

			doc.remove(0, doc.getLength());
			setCaretPosition(0);
			return this;
		}
		catch (RuntimeException err) {
			throw err;
		}
		catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
}
