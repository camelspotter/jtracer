package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import static java.awt.event.KeyEvent.*;

public class MenuBar extends JMenuBar implements ActionListener,
                                                 PropertyChangeListener,
                                                 AutoInjectable
{
	private static final long serialVersionUID = 7115815907319975508L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;

	@Service
	protected UtilityService utilitySvc;


	protected Boolean[] toggleStates = { true, true, false };

	protected ActionListener handler;


	public MenuBar()
	{
		super();
		selfInject();
	}

	public MenuBar(ActionListener owner)
	{
		super();
		selfInject();
		handler = owner;

		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		Font font = componentSvc.getFont("component");
		Color foreground = componentSvc.getForegroundColor("component");

		/* Create and populate the 'Service' menu */
		JMenu menu = new JMenu("Service");
		menu.setFont(font);
		menu.setForeground(foreground);
		menu.setMnemonic(VK_S);

		menu.add(createItem("Start", VK_S, VK_S));
		menu.add(createItem("Stop", VK_T, VK_T));
		menu.add(createItem("Restart", VK_R, VK_R));
		menu.add(createItem("Clear log", VK_L, VK_L));
		menu.addSeparator();

		menu.add(createItem("Quit", VK_Q, VK_Q));
		add(menu);

		/* Create and populate the 'View' menu */
		menu = new JMenu("View");
		menu.setFont(font);
		menu.setForeground(foreground);
		menu.setMnemonic(VK_V);

		menu.add(createToggle("Toolbar", 0, VK_T));
		menu.add(createToggle("Statusbar", 1, VK_S));
		menu.add(createToggle("Always on top", 2, VK_A));
		menu.addSeparator();

		menu.add(createItem("Find...", VK_F, VK_F));
		menu.add(createItem("Preferences...", VK_P, VK_P));
		menu.addSeparator();

		menu.add(createItem("Full screen", VK_U, VK_F11));
		add(menu);

		/* Create and populate the 'Session' menu */
		menu = new JMenu("Session");
		menu.setFont(font);
		menu.setForeground(foreground);
		menu.setMnemonic(VK_I);

		menu.add(createItem("Select previous", VK_P, VK_COMMA));
		menu.add(createItem("Select next", VK_N, VK_PERIOD));
		menu.add(createItem("Close", VK_C, VK_W));
		menu.addSeparator();

		menu.add(createItem("Cascade", VK_S, VK_SLASH));
		menu.add(createItem("Minimize all", VK_M, VK_DOWN));
		menu.add(createItem("Restore all", VK_R, VK_UP));
		menu.add(createItem("Close all", VK_E, VK_E));
		add(menu);

		/* Create and populate the 'Help' menu */
		menu = new JMenu("Help");
		menu.setFont(font);
		menu.setForeground(foreground);
		menu.setMnemonic(VK_H);

		menu.add(createItem("Online documentation", VK_D, 0));
		menu.add(createItem("Bug tracker", VK_B, 0));
		menu.add(createItem("Submit feedback", VK_F, 0));
		menu.add(createItem("Check for updates", VK_U, 0));
		menu.addSeparator();

		menu.add(createItem("About " + registrySvc.get("name"), VK_A, VK_F1));
		add(menu);
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		try {
			String cmd = event.getActionCommand();

			int index = 0;
			if (cmd.equals("Statusbar")) {
				index++;
			}
			else if (cmd.equals("Always on top")) {
				index += 2;
			}

			/* Change the state and icon of the toggle item */
			boolean state = !toggleStates[index];
			toggleStates[index] = state;
			String icon = ((state) ? "on" : "off") + "16.png";

			getMenu(1).getItem(index)
			          .setIcon(utilitySvc.loadIcon(icon));
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}
	}

	protected JMenuItem createItem(String text, Integer mnemonic, Integer accelerator)
	{
		JMenuItem retval = new JMenuItem(text + "  ");
		retval.setMnemonic(mnemonic);

		retval.setFont(componentSvc.getFont("component"));
		retval.setForeground(componentSvc.getForegroundColor("component"));

		String param = registrySvc.get("click-delay");
		if (param != null) {
			retval.setMultiClickThreshhold(Integer.parseInt(param));
		}

		if (accelerator != 0) {
			int modifiers = 0;
			if (accelerator < VK_F1 || accelerator > VK_F12) {
				modifiers |= InputEvent.CTRL_DOWN_MASK;
			}

			retval.setAccelerator(KeyStroke.getKeyStroke(accelerator, modifiers));
		}

		/* The action command is the item caption with the trailing dots trimmed */
		text = text.replace('.', ' ').trim();
		retval.setActionCommand(text);
		retval.addActionListener(handler);

		String icon = text.toLowerCase().replace(' ', '_') + "16.png";
		retval.setIcon(utilitySvc.loadIcon(icon));
		return retval;
	}

	protected JMenuItem createToggle(String text, Integer index, Integer mnemonic)
	{
		JMenuItem retval = new JMenuItem(text);
		retval.setMnemonic(mnemonic);

		retval.setFont(componentSvc.getFont("component"));
		retval.setForeground(componentSvc.getForegroundColor("component"));

		String param = registrySvc.get("click-delay");
		if (param != null) {
			retval.setMultiClickThreshhold(Integer.parseInt(param));
		}

		retval.setActionCommand(text);
		retval.addActionListener(handler);
		retval.addActionListener(this);

		String icon = ((toggleStates[index]) ? "on" : "off") + "16.png";
		retval.setIcon(utilitySvc.loadIcon(icon));
		return retval;
	}

	public Boolean getToggleState(Integer index)
	{
		return toggleStates[index];
	}

	public MenuBar listSessions(List<Session> sessions, Integer selected)
	{
		JMenu menu = getMenu(2);
		while (menu.getMenuComponentCount() > 8) {
			menu.remove(8);
		}

		int size = sessions.size();
		if (size == 0) {
			return this;
		}

		menu.addSeparator();

		String param = registrySvc.get("click-delay");
		Integer delay = null;
		if (param != null) {
			delay = Integer.parseInt(param);
		}

		Font font = componentSvc.getFont("component");
		Color foreground = componentSvc.getForegroundColor("component");

		ImageIcon onIcon = utilitySvc.loadIcon("on16.png");
		ImageIcon offIcon = utilitySvc.loadIcon("voidrect16.png");

		for (int i = 0; i < size; i++) {
			JFrame window = sessions.get(i);

			JMenuItem item = new JMenuItem(window.getTitle() + "  ");
			item.setFont(font);
			item.setForeground(foreground);
			if (delay != null) {
				item.setMultiClickThreshhold(delay);
			}

			if (i < 10) {
				item.setAccelerator(KeyStroke.getKeyStroke(VK_0 + i, InputEvent.ALT_DOWN_MASK));
			}

			item.setIcon((i == selected) ? onIcon : offIcon);
			item.setActionCommand("Select session " + i);
			item.addActionListener(handler);
			menu.add(item);
		}

		return this;
	}

	public void propertyChange(PropertyChangeEvent event)
	{
		loggerSvc.debug(getClass(), event.toString());

		try {
			String key = event.getPropertyName();
			Object value = event.getNewValue();

			for (int i = getMenuCount() - 1; i >= 0; i--) {
				JMenu menu = getMenu(i);

				for (int j = menu.getMenuComponentCount() - 1; j >= 0; j--) {
					Component c = menu.getMenuComponent(j);
					if (c instanceof JMenuItem) {
						renderItem(c, i, j, key, value);
					}
				}
			}
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}
	}

	protected MenuBar renderItem(Component c, Integer menu, Integer index, String key, Object value)
	{
		Boolean enabled = (Boolean) value;

		if (key.equals("isServing")) {
			if (menu != 0) {
				return this;
			}

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
			switch (menu) {
			case 1:
				if (index == 4) {
					c.setEnabled(enabled);
				}

				break;

			case 2:
				if (index < 8) {
					c.setEnabled(enabled);
				}
			}
		}

		return this;
	}

	public MenuBar setSelectedSession(Integer index)
	{
		JMenu menu = getMenu(2);

		ImageIcon onIcon = utilitySvc.loadIcon("on16.png");
		ImageIcon offIcon = utilitySvc.loadIcon("voidrect16.png");

		for (int i = menu.getMenuComponentCount() - 1; i >= 9; i--) {
			JMenuItem item = (JMenuItem) menu.getMenuComponent(i);
			item.setIcon((i - 9 == index) ? onIcon : offIcon);
		}

		return this;
	}
}
