package net.libcsdbg.jtracer.gui.component;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.annotation.Note;
import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.gui.container.MainFrame;
import net.libcsdbg.jtracer.gui.container.Session;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
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
import static net.libcsdbg.jtracer.gui.component.MenuBar.Type.*;
import static net.libcsdbg.jtracer.service.util.PlatformDetectionApi.Platform;

public class MenuBar extends JMenuBar implements ActionListener,
                                                 AutoInjectable,
                                                 PropertyChangeListener
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


	protected ActionListener handler;

	protected Boolean[] toggleStates = { true, true, false };


	private MenuBar()
	{
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

		menu.add(createItem("Quit...", VK_Q, VK_Q));
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

		menu.add(createItem("Current log level", VK_C, 0));
		menu.add(createItem("Lower log level", VK_L, VK_D));
		menu.add(createItem("Higher log level", VK_H, VK_U));
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
		menu.add(createItem("Check for updates", VK_U, 0));
		menu.addSeparator();

		menu.add(createItem("Bug tracker", VK_B, 0));
		menu.add(createItem("Submit feedback", VK_F, 0));
		menu.add(createItem("Join forum", VK_O, 0));
		menu.addSeparator();

		String param =
			registrySvc.getOrDefault("full-name", MainFrame.Config.name)
			           .trim();

		menu.add(createItem("About " + param + "... ", VK_A, VK_F1));
		add(menu);
	}

	@Note("Only the toggle menu items are registered with this action listener")
	@Override
	public void actionPerformed(ActionEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
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

		getMenu(view.ordinal()).getItem(index)
		                       .setIcon(utilitySvc.loadIcon(icon));
	}

	@Factory(Factory.Type.POJO)
	protected JMenuItem createItem(String text, Integer mnemonic, Integer accelerator)
	{
		JMenuItem retval = new JMenuItem(text + "  ");

		retval.setFont(componentSvc.getFont("component"));
		retval.setForeground(componentSvc.getForegroundColor("component"));

		String param = registrySvc.get("click-delay");
		if (param != null) {
			retval.setMultiClickThreshhold(Integer.parseInt(param.trim()));
		}

		retval.setMnemonic(mnemonic);
		if (accelerator != 0) {
			int modifiers = 0;
			if (accelerator < VK_F1 || accelerator > VK_F12) {
				modifiers |= InputEvent.CTRL_DOWN_MASK;
			}

			retval.setAccelerator(KeyStroke.getKeyStroke(accelerator, modifiers));
		}

		/* The action command is the item caption with the trailing dots and dash-separated suffix trimmed */
		int index = text.indexOf('-');
		if (index >= 1) {
			text = text.substring(0, index);
		}

		text = text.replace('.', ' ').trim();
		retval.setActionCommand(text);
		retval.addActionListener(handler);

		String icon = text.toLowerCase().replace(' ', '_') + "16.png";
		retval.setIcon(utilitySvc.loadIcon(icon));
		return retval;
	}

	@Factory(Factory.Type.POJO)
	protected JMenuItem createToggle(String text, Integer index, Integer mnemonic)
	{
		JMenuItem retval = new JMenuItem(text);

		retval.setFont(componentSvc.getFont("component"));
		retval.setForeground(componentSvc.getForegroundColor("component"));

		String param = registrySvc.get("click-delay");
		if (param != null) {
			retval.setMultiClickThreshhold(Integer.parseInt(param.trim()));
		}

		retval.setMnemonic(mnemonic);
		retval.setActionCommand(text);
		retval.addActionListener(handler);
		retval.addActionListener(this);

		String icon = (getToggleState(index) ? "on" : "off") + "16.png";
		retval.setIcon(utilitySvc.loadIcon(icon));
		return retval;
	}

	public Boolean getToggleState(Integer index)
	{
		if (index < 0 || index > 2) {
			return false;
		}

		return toggleStates[index];
	}

	@Note("Currently called only from within event handlers")
	public MenuBar listSessions(List<Session> sessions, Integer selected)
	{
		JMenu menu = getMenu(session.ordinal());
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
			delay = Integer.parseInt(param.trim());
		}

		Font font = componentSvc.getFont("component");
		Color foreground = componentSvc.getForegroundColor("component");

		ImageIcon onIcon = utilitySvc.loadIcon("on16.png");
		ImageIcon offIcon = utilitySvc.loadIcon("voidrect16.png");

		for (int i = 0; i < size; i++) {
			Session window = sessions.get(i);

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

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		for (int i = getMenuCount() - 1; i >= 0; i--) {
			JMenu menu = getMenu(i);

			for (int j = menu.getMenuComponentCount() - 1; j >= 0; j--) {
				Component c = menu.getMenuComponent(j);

				if (c instanceof JMenuItem) {
					renderItem(c, i, j, event.getPropertyName(), event.getNewValue());
				}
			}
		}
	}

	protected MenuBar renderItem(Component c, Integer menu, Integer item, String key, Object value)
	{
		boolean enabled = (Boolean) value;

		if (key.equals("isServing")) {
			if (menu != service.ordinal()) {
				return this;
			}

			switch (item) {
			case 0:
				c.setEnabled(!enabled);
				break;

			case 1:
			case 2:
				c.setEnabled(enabled);
			}
		}

		else if (key.equals("hasClients")) {
			if (menu == view.ordinal()) {
				if (item == 4) {
					c.setEnabled(enabled);
				}
			}

			else if (menu == session.ordinal()) {
				if (item < 8) {
					c.setEnabled(enabled);
				}
			}
		}

		return this;
	}

	@Note("Currently called only from within event handlers")
	public MenuBar setSelectedSession(Integer index)
	{
		JMenu menu = getMenu(session.ordinal());

		ImageIcon onIcon = utilitySvc.loadIcon("on16.png");
		ImageIcon offIcon = utilitySvc.loadIcon("voidrect16.png");

		index += 9;
		for (int i = menu.getMenuComponentCount() - 1; i >= 9; i--) {
			JMenuItem item = (JMenuItem) menu.getMenuComponent(i);
			item.setIcon((i == index) ? onIcon : offIcon);
		}

		return this;
	}


	public static enum Type
	{
		service,

		view,

		session,

		help
	}
}
