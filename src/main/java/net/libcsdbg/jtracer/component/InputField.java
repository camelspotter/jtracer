package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionListener;

public class InputField extends JTextField implements AutoInjectable
{
	private static final long serialVersionUID = 8335665145331122729L;


	@Service
	protected ComponentService componentSvc;


	protected String grammar;


	public InputField()
	{
		this("");
	}

	public InputField(String text)
	{
		super(text);
		setProperties();
	}

	public InputField(String command, ActionListener handler)
	{
		this("", command, handler);
	}

	public InputField(String text, String command, ActionListener handler)
	{
		super(text);

		setActionCommand(command);
		addActionListener(handler);

		setProperties();
	}

	protected Document createDefaultModel()
	{
		return new RegularExpressionDocument(grammar);
	}

	public String getGrammar()
	{
		return grammar;
	}

	public InputField setGrammar(String grammar)
	{
		this.grammar = grammar;
		setDocument(createDefaultModel());
		return this;
	}

	protected InputField setProperties()
	{
		selfInject();
		String widget = "component";

		setFont(componentSvc.getFont(widget));
		setForeground(componentSvc.getForegroundColor(widget));
		setCaretColor(componentSvc.getCaretColor(widget));

		setSelectionColor(componentSvc.getBackgroundColor(widget + "-selection"));
		setSelectedTextColor(componentSvc.getForegroundColor(widget + "-selection"));

		setDragEnabled(componentSvc.isEnabled("input-field-drag-enabled"));
		setLocale(componentSvc.getLocale("input-field"));

		return this;
	}


	public static class RegularExpressionDocument extends PlainDocument
	{
		private static final long serialVersionUID = 4719047964101045906L;


		protected String grammar;


		public RegularExpressionDocument(String grammar)
		{
			super();

			if (grammar == null) {
				this.grammar = "";
			}
			else {
				this.grammar = grammar.trim();
			}
		}

		@Override
		public void insertString(int offset, String input, AttributeSet set) throws BadLocationException
		{
			if (input.length() == 0) {
				return;
			}

			if (grammar.length() > 0 && !input.matches(grammar)) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}

			super.insertString(offset, input, set);
		}
	}
}
