package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Console extends JTextArea implements KeyListener,
                                                  MouseListener,
                                                  AutoInjectable
{
	private static final long serialVersionUID = 2993991645909760546L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;


	protected String prompt;


	public Console()
	{
		super();
		selfInject();
		configure();
		activate();
	}

	protected Console configure()
	{
		selfInject();
		String widget = "console";

		setFont(componentSvc.getFont(widget));
		setForeground(componentSvc.getForegroundColor(widget));
		setBackground(componentSvc.getBackgroundColor(widget));
		setCaretColor(componentSvc.getCaretColor(widget));

		setSelectionColor(componentSvc.getBackgroundColor(widget + "-selection"));
		setSelectedTextColor(componentSvc.getForegroundColor(widget + "-selection"));

		setDragEnabled(componentSvc.isEnabled("console-drag-enabled"));
		setLocale(componentSvc.getLocale("console"));
		setMargin(componentSvc.getInsets("console"));

		return this;
	}

	protected Console activate()
	{
		String param = registrySvc.get("console-prompt");
		if (param == null) {
			prompt = Config.prompt;
		}
		else {
			prompt = param.trim() + " ";
		}

		setText(prompt);

		for (MouseListener listener : getMouseListeners()) {
			removeMouseListener(listener);
		}

		addKeyListener(this);
		addMouseListener(this);

		return this;
	}

	public Console interpret(String cmd)
	{
		if (cmd == null) {
			return this;
		}

		cmd = cmd.trim();
		if (cmd.length() == 0) {
			return this;
		}

		loggerSvc.info(getClass(), "Interpreting command '" + cmd + "'");
		return this;
	}

	@Override
	public void keyPressed(KeyEvent event)
	{
		if (event.isConsumed()) {
			return;
		}

		loggerSvc.trace(getClass(), event.toString());

		loggerSvc.debug(getClass(), event.toString());
		loggerSvc.debug(getClass(), "Code: " + event.getKeyCode());

		int code = event.getKeyCode();
		switch (code) {
		case KeyEvent.VK_UP:
		case KeyEvent.VK_PAGE_UP:
		case KeyEvent.VK_NUMPAD8:
			event.consume();
			break;

		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_NUMPAD4:
			try {
				int offset = getLineStartOffset(getLineCount() - 1);
				int position = getCaretPosition() - offset;
				if (position <= prompt.length()) {
					event.consume();
				}
			}
			catch (Throwable err) {
				event.consume();
			}

			break;

		case KeyEvent.VK_HOME:
			try {
				int offset = getLineStartOffset(getLineCount() - 1);
				setCaretPosition(offset + prompt.length());
				event.consume();
			}
			catch (Throwable err) {
				event.consume();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent event)
	{
		if (event.isConsumed()) {
			return;
		}

		loggerSvc.trace(getClass(), event.toString());

		int code = event.getKeyCode();
		if (code == KeyEvent.VK_ENTER) {
			String text = getText();
			String cmd = text.substring(text.lastIndexOf(prompt) + prompt.length()).trim();
			interpret(cmd);
			append(prompt);
		}
	}

	@Override
	public void keyTyped(KeyEvent event)
	{
		if (event.isConsumed()) {
			return;
		}

		loggerSvc.trace(getClass(), event.toString());
	}

	@Override
	public void mouseClicked(MouseEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
		event.consume();
	}

	@Override
	public void mouseEntered(MouseEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
		event.consume();
	}

	@Override
	public void mouseExited(MouseEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
		event.consume();
	}

	@Override
	public void mousePressed(MouseEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
		event.consume();
	}

	@Override
	public void mouseReleased(MouseEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
		event.consume();
	}


	public static class Config
	{
		public static String prompt = "jTracer> ";
	}
}
