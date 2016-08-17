package net.libcsdbg.jtracer.gui.component;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.gui.container.MainFrame;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ToolBar extends JToolBar implements AutoInjectable,
                                                 PropertyChangeListener
{
	private static final long serialVersionUID = -1810400400518571298L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;


	protected ActionListener handler;


	private ToolBar()
	{
	}

	public ToolBar(ActionListener owner)
	{
		super();
		selfInject();
		handler = owner;

		String param =
			registrySvc.getOrDefault("full-name", MainFrame.Config.name)
			           .trim();

		/* Setting a new border removes the float handle. To properly set the border, get the current (probably matte) border and set its properties */
		setName(param + " tools");
		setRollover(false);

		add(createTool("Start"));
		add(createTool("Stop"));
		add(createTool("Restart"));
		addSeparator();

		add(createTool("Select previous"));
		add(createTool("Select next"));
		add(createTool("Close"));
		addSeparator();

		add(createTool("Cascade"));
		add(createTool("Minimize all"));
		add(createTool("Restore all"));
		add(createTool("Close all"));
		addSeparator();

		add(createTool("Find"));
		add(createTool("Preferences"));
	}

	@Factory(Factory.Type.POJO)
	protected Button createTool(String command)
	{
		String icon = command.toLowerCase().replace(' ', '_') + "24.png";

		Button retval = new Button(icon, command, handler);
		retval.setMargin(componentSvc.getInsets("component"));

		return retval;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		for (int i = getComponentCount() - 1; i >= 0; i--) {
			Component c = getComponent(i);

			if (c instanceof Button) {
				renderTool(c, i, event.getPropertyName(), event.getNewValue());
			}
		}
	}

	protected ToolBar renderTool(Component c, Integer toolIndex, String key, Object value)
	{
		boolean enabled = (Boolean) value;

		if (key.equals("isServing")) {
			switch (toolIndex) {
			case 0:
				c.setEnabled(!enabled);
				break;

			case 1:
			case 2:
				c.setEnabled(enabled);
			}
		}
		else if (key.equals("hasClients")) {
			if (toolIndex >= 4 && toolIndex <= 13) {
				c.setEnabled(enabled);
			}
		}

		return this;
	}
}
