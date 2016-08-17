package net.libcsdbg.jtracer.gui.component;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.annotation.Note;
import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class TraceToolBar extends JToolBar implements AutoInjectable,
                                                      PropertyChangeListener
{
	private static final long serialVersionUID = 5454066943250445711L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected UtilityService utilitySvc;


	private TraceToolBar()
	{
	}

	@Note("Handler 0 is the session window, 1 is the main window")
	public TraceToolBar(ActionListener... handlers)
	{
		super();
		selfInject();

		setName("Session tools");
		setRollover(false);

		add(createTool("Remove trace", handlers[0]));
		add(createTool("Lock", handlers[0]));
		addSeparator();

		add(createTool("Select previous", handlers[1]));
		add(createTool("Select next", handlers[1]));
		add(createTool("Close", handlers[1]));
		addSeparator();

		add(createTool("Find", handlers[0]));
	}

	@Factory(Factory.Type.POJO)
	protected Button createTool(String command, ActionListener handler)
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

	protected TraceToolBar renderTool(Component c, Integer toolIndex, String key, Object value)
	{
		boolean enabled = (Boolean) value;

		if (key.equals("hasTraces")) {
			if (toolIndex == 0 || toolIndex == 7) {
				c.setEnabled(enabled);
			}
		}
		else if (key.equals("isLocked")) {
			if (toolIndex != 1) {
				return this;
			}

			Button tool = (Button) c;
			if (enabled) {
				tool.setIcon(utilitySvc.loadIcon("unlock24.png"));
				tool.setToolTipText("Unlock");
			}
			else {
				tool.setIcon(utilitySvc.loadIcon("lock24.png"));
				tool.setToolTipText("Lock");
			}
		}

		return this;
	}
}
