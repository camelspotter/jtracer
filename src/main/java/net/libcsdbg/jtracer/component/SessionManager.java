package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.ApplicationCore;
import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.graphics.value.GridPresets;
import net.libcsdbg.jtracer.service.log.LoggerService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SessionManager extends Component implements WindowListener,
                                                         AutoInjectable
{
	private static final long serialVersionUID = -560110208869609605L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;


	protected Integer current;

	protected final JFrame owner;

	protected final List<Session> sessions;


	public SessionManager()
	{
		super();
		selfInject();

		current = -1;
		owner = null;
		sessions = null;
	}

	public SessionManager(JFrame parent)
	{
		super();
		selfInject();

		current = -1;
		owner = parent;
		sessions = new ArrayList<>();

		addPropertyChangeListener("sessionCount", (PropertyChangeListener) owner);
		addPropertyChangeListener("currentSession", (PropertyChangeListener) owner);
	}

	public SessionManager cascade()
	{
		for (int i = 0, size = sessions.size(); i < size; i++) {
			setClientPosition(i);
		}

		return this;
	}

	public SessionManager disposeAll()
	{
		while (sessions.size() > 0) {
			Session first = sessions.remove(0);
			first.removeWindowListener(this);
			first.quit()
			     .dispose();
		}

		current = -1;
		firePropertyChange("sessionCount", null, 0);

		return this;
	}

	public SessionManager disposeCurrent()
	{
		if (current >= 0) {
			sessions.get(current)
			        .dispose();
		}

		return this;
	}

	public Integer getCurrent()
	{
		return current;
	}

	public Integer getSessionCount()
	{
		return sessions.size();
	}

	public List<Session> getSessions()
	{
		return sessions;
	}

	public Integer getTraceCount()
	{
		int count = 0;
		for (Session session : sessions) {
			count += session.getTraceCount();
		}

		return count;
	}

	public SessionManager register(Socket sock)
	{
		Session session = new Session(owner, sock);
		session.addWindowListener(this);

		sessions.add(session);
		current = sessions.size() - 1;

		setClientPosition(current);
		session.setVisible(true);
		session.toFront();
		session.setAlwaysOnTop(owner.isAlwaysOnTop());

		return this;
	}

	public SessionManager setAlwaysOnTop(Integer index, Boolean alwaysOnTop)
	{
		int size = sessions.size();
		if (index < 0) {
			for (index = 0; index < size; index++) {
				sessions.get(index)
				        .setAlwaysOnTop(alwaysOnTop);
			}

			return this;
		}

		if (index < size) {
			sessions.get(index)
			        .setAlwaysOnTop(alwaysOnTop);
		}

		return this;
	}

	public SessionManager setClientPosition(Integer index)
	{
		if (index < 0) {
			return this;
		}

		GridPresets presets = componentSvc.getGridPresets("desktop");

		int rowSize = presets.rowSize().get();
		int row = (index + rowSize) / rowSize;
		int column = index % rowSize;

		int step = presets.step().get();
		int x = presets.baseX().get() + step * column;
		int y = presets.baseY().get() + step * (row + column);

		sessions.get(index)
		        .setLocation(x, y);

		return this;
	}

	public SessionManager setCurrent(Integer selected)
	{
		if (selected < 0 || selected >= sessions.size()) {
			return this;
		}

		Session session = sessions.get(selected);
		session.setIconified(false);
		session.toFront();

		current = selected;
		return this;
	}

	public SessionManager setIconified(final Boolean iconified)
	{
		/* Defer execution to another thread */
		Runnable worker = () -> {
			synchronized (sessions) {
				int previous = current;
				for (Session session : sessions) {
					session.setIconified(iconified);

					try {
						Thread.sleep(Config.iconificationDelay);
					}
					catch (InterruptedException ignored) {
					}
				}

				/* When a window is restored, it's activated by the OS window manager */
				if (!iconified && previous >= 0) {
					sessions.get(previous)
					        .toFront();
				}
			}
		};

		ThreadGroup group =
			Thread.currentThread()
			      .getThreadGroup();

		Thread runner = new Thread(group, worker, Config.iconificationWorkerName);
		runner.setUncaughtExceptionHandler(ApplicationCore.getCurrentApplicationCore()
		                                                  .getUncaughtExceptionHandler());
		runner.start();

		return this;
	}

	public SessionManager shiftSelection(Integer step)
	{
		/* Quantize step */
		step = (step < 0) ? -1 : 1;
		Session previous = sessions.get(current);
		Session selected;

		/* This loop stops when either a non-iconified window is selected or (if all windows are iconified) when a full circle is completed */
		do {
			current += step;
			if (current < 0) {
				current = sessions.size() - 1;
			}
			else if (current > sessions.size() - 1) {
				current = 0;
			}

			selected = sessions.get(current);
			if (selected.isIconified()) {
				continue;
			}

			selected.toFront();
			break;
		}
		while (!previous.equals(selected));

		return this;
	}

	@Override
	public void windowActivated(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		JFrame source = (JFrame) event.getWindow();
		if (source.equals(owner)) {
			return;
		}

		for (int i = 0, size = sessions.size(); i < size; i++) {
			Session session = sessions.get(i);

			if (session.equals(source)) {
				current = i;
				firePropertyChange("currentSession", null, current);
				break;
			}
		}
	}

	@Override
	public void windowClosed(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		JFrame source = (JFrame) event.getWindow();
		if (source.equals(owner)) {
			return;
		}

		Session session = sessions.remove(current.intValue());
		session.quit();

		int size = sessions.size();
		firePropertyChange("sessionCount", null, size);

		if (size == 0) {
			current = -1;
			return;
		}
		else if (current == size) {
			current--;
		}

		session = sessions.get(current);
		if (session.isIconified()) {
			shiftSelection(1);
		}
		else {
			session.toFront();
		}
	}

	@Override
	public void windowClosing(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		JFrame source = (JFrame) event.getWindow();
		if (!source.equals(owner) && current >= 0 && current < sessions.size()) {
			sessions.get(current)
			        .dispose();
		}
	}

	@Override
	public void windowDeactivated(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
	}

	@Override
	public void windowDeiconified(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		JFrame source = (JFrame) event.getWindow();
		if (source.equals(owner)) {
			setIconified(false);
		}
	}

	@Override
	public void windowIconified(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		JFrame source = (JFrame) event.getWindow();
		if (source.equals(owner)) {
			setIconified(true);
		}
	}

	@Override
	public void windowOpened(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		JFrame source = (JFrame) event.getWindow();
		if (!source.equals(owner)) {
			firePropertyChange("sessionCount", null, sessions.size());
		}
	}


	public static class Config
	{
		public static Integer iconificationDelay = 128;

		public static String iconificationWorkerName = "Session Minimizing/Maximizing Worker Thread";
	}
}
