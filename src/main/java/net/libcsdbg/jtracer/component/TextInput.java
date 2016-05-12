package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.event.ActionListener;

public class TextInput extends JTextField implements AutoInjectable
{
	private static final long serialVersionUID = -536561545468655357L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;

	@Service
	protected UtilityService utilitySvc;


	private TextInput()
	{
	}

	public TextInput(String command, String text, ActionListener handler)
	{
		super();
		selfInject();
		setActionCommand(command);
		addActionListener(handler);
	}

	public TextInput(String text, ActionListener handler)
	{
		super(text);
		selfInject();

		setFont(componentSvc.getFont("component"));
		setForeground(componentSvc.getForegroundColor("component"));

		setActionCommand(text);
		addActionListener(handler);
	}

	public TextInput(ActionListener handler)
	{
		super();
		selfInject();

		setFont(componentSvc.getFont("component"));
		setForeground(componentSvc.getForegroundColor("component"));

		//setActionCommand(text);
		//addActionListener(handler);
	}
}
