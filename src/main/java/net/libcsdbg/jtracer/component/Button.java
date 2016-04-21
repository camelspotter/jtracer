package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Button extends JButton implements MouseListener,
                                               AutoInjectable
{
	private static final long serialVersionUID = -427470634379746246L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;

	@Service
	protected UtilityService utilitySvc;


	public Button()
	{
		super();
		selfInject();
	}

	public Button(String icon, String command, ActionListener handler)
	{
		super();
		selfInject();
		setIcon(utilitySvc.loadIcon(icon));

		String param = registrySvc.get("click-delay");
		if (param != null) {
			setMultiClickThreshhold(Integer.parseInt(param));
		}

		setToolTipText(command);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setRolloverEnabled(false);

		setActionCommand(command);
		addActionListener(handler);
		addMouseListener(this);
	}

	public Button(String icon, String text, Integer horizontalAlignment, Integer verticalAlignment, ActionListener handler)
	{
		super(text);
		selfInject();

		setIcon(utilitySvc.loadIcon(icon));
		setFont(componentSvc.getFont("component"));
		setForeground(componentSvc.getForegroundColor("component"));

		String param = registrySvc.get("click-delay");
		if (param != null) {
			setMultiClickThreshhold(Integer.parseInt(param));
		}

		setHorizontalTextPosition(horizontalAlignment);
		setVerticalTextPosition(verticalAlignment);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setRolloverEnabled(false);

		setActionCommand(text);
		addActionListener(handler);
		addMouseListener(this);
	}

	public Button(String text, ActionListener handler)
	{
		super(text);
		selfInject();

		setFont(componentSvc.getFont("component"));
		setForeground(componentSvc.getForegroundColor("component"));

		String param = registrySvc.get("click-delay");
		if (param != null) {
			setMultiClickThreshhold(Integer.parseInt(param));
		}

		setContentAreaFilled(false);
		setActionCommand(text);
		addActionListener(handler);
	}

	@Override
	public void mouseClicked(MouseEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
	}

	@Override
	public void mouseEntered(MouseEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		if (isEnabled()) {
			setBorderPainted(true);
		}
	}

	@Override
	public void mouseExited(MouseEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
		setBorderPainted(false);
	}

	@Override
	public void mousePressed(MouseEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
	}

	@Override
	public void mouseReleased(MouseEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		/* If it is an icon button, hide its border if it gets disabled */
		if (!enabled && getIcon() != null) {
			setBorderPainted(false);
		}

		super.setEnabled(enabled);
	}
}
