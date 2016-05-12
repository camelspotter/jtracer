package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.event.ActionListener;

public class InputField extends JTextField implements AutoInjectable
{
	private static final long serialVersionUID = 8335665145331122729L;


	@Service
	protected ComponentService componentSvc;


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
}
