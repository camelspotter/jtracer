package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class TraceToolBar extends JToolBar implements PropertyChangeListener,
                                                      AutoInjectable
{
	private static final long serialVersionUID = -2420200801025032444L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;


	public TraceToolBar()
	{
		super();
		selfInject();
	}

	public TraceToolBar(ActionListener... listeners)
	{
		super();
		selfInject();

		setName("Session tools");
		setRollover(false);

		add(createTool("Remove trace", listeners));
		add(createTool("Lock", listeners[0]));
		addSeparator();

		add(createTool("Select previous", listeners[1]));
		add(createTool("Select next", listeners[1]));
		add(createTool("Close", listeners[1]));
		addSeparator();

		add(createTool("Find", listeners[0]));
	}

	@Factory
	protected Button createTool(String command, ActionListener... listeners)
	{
		String name = command.toLowerCase().replace(' ', '_') + "24.png";

		Button retval = new Button(name, command, listeners[0]);
		retval.setMargin(componentSvc.getInsets("component"));

		for (int i = 1, size = listeners.length; i < size; i++) {
			retval.addActionListener(listeners[i]);
		}

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

	protected TraceToolBar renderTool(Component c, Integer index, String key, Object value)
	{
		Boolean enabled = (Boolean) value;

		if (key.equals("hasTraces")) {
			switch (index) {
			case 0:
			case 7:
				c.setEnabled(enabled);
			}
		}

		return this;
	}
}