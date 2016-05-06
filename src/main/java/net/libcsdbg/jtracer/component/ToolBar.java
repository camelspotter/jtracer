package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ToolBar extends JToolBar implements PropertyChangeListener,
                                                 AutoInjectable
{
	private static final long serialVersionUID = -1810400400518571298L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;


	protected ActionListener handler;


	public ToolBar()
	{
		super();
		selfInject();
	}

	public ToolBar(ActionListener owner)
	{
		super();
		selfInject();
		handler = owner;

		/* Setting a new border removes the float handle. To properly set the border, get the current (probably matte) border and set its properties */
		setName(registrySvc.get("full-name") + " tools");
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

	@Factory
	protected Button createTool(String command)
	{
		String name = command.toLowerCase().replace(' ', '_') + "24.png";

		Button retval = new Button(name, command, handler);
		retval.setMargin(componentSvc.getInsets("component"));

		return retval;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		String key = event.getPropertyName();
		Object value = event.getNewValue();

		for (int i = getComponentCount() - 1; i >= 0; i--) {
			Component c = getComponent(i);
			if (c instanceof Button) {
				renderTool(c, i, key, value);
			}
		}
	}

	protected ToolBar renderTool(Component c, Integer index, String key, Object value)
	{
		Boolean enabled = (Boolean) value;

		if (key.equals("isServing")) {
			switch (index) {
			case 0:
				c.setEnabled(!enabled);
				break;

			case 1:
			case 2:
				c.setEnabled(enabled);
			}
		}
		else if (key.equals("hasClients")) {
			if (index >= 4 && index <= 13) {
				c.setEnabled(enabled);
			}
		}

		return this;
	}
}
