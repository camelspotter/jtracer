package net.libcsdbg.jtracer.gui.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.event.ActionListener;

public class Checkbox extends JCheckBox implements AutoInjectable
{
	private static final long serialVersionUID = 4511539694762043430L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected RegistryService registrySvc;

	@Service
	protected UtilityService utilitySvc;


	private Checkbox()
	{
	}

	public Checkbox(String text, ActionListener handler)
	{
		this(text, handler, false);
	}

	public Checkbox(String text, ActionListener handler, Boolean selected)
	{
		super(text, selected);
		selfInject();

		setIcon(utilitySvc.loadIcon("off16.png"));
		setSelectedIcon(utilitySvc.loadIcon("on16.png"));

		setFont(componentSvc.getFont("component"));
		setForeground(componentSvc.getForegroundColor("component"));

		String param = registrySvc.get("click-delay");
		if (param != null) {
			setMultiClickThreshhold(Integer.parseInt(param.trim()));
		}

		setActionCommand(text);
		addActionListener(handler);
	}
}
