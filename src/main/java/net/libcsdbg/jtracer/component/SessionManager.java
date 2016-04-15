package net.libcsdbg.jtracer.component;

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


	protected JFrame owner;

	protected List<Session> sessions;

	protected Integer current;


	public SessionManager()
	{
		super();
		selfInject();
	}

	public SessionManager(JFrame parent)
	{
		super();
		selfInject();

		owner = parent;
		sessions = new ArrayList<>();
		current = -1;

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
			Session current = sessions.remove(0);
			current.removeWindowListener(this);
			current.dispose();
		}

		current = -1;
		firePropertyChange("sessionCount", null, 0);

		return this;
	}

	public SessionManager disposeCurrent()
	{
		sessions.get(current)
		        .quit()
		        .dispose();

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
		if (index >= 0 && index < size) {
			sessions.get(index)
			        .setAlwaysOnTop(alwaysOnTop);

			return this;
		}

		for (index = 0; index < size; index++) {
			sessions.get(index)
			        .setAlwaysOnTop(alwaysOnTop);
		}

		return this;
	}

	public SessionManager setClientPosition(Integer index)
	{
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

		Session current = sessions.get(selected);
		current.setIconified(false);
		current.toFront();

		return this;
	}

	public SessionManager setIconified(final Boolean iconified)
	{
		/* Defer execution to another thread */
		Runnable worker = () -> {
			synchronized (this) {
				int previous = current;
				for (Session session : sessions) {
					session.setIconified(iconified);

					try {
						Thread.sleep(50);
					}
					catch (InterruptedException ignored) {
					}
				}

				/* When a window is restored, it's activated by the OS window manager */
				if (!iconified) {
					sessions.get(previous)
					        .toFront();
				}
			}
		};

		new Thread(worker, "Session Worker Thread").start();
		return this;
	}

	public SessionManager shiftSelection(Integer step)
	{
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

	public void windowActivated(WindowEvent event)
	{
		loggerSvc.debug(getClass(), event.toString());

		try {
			JFrame source = (JFrame) event.getWindow();
			if (source.equals(owner)) {
				return;
			}

			for (int i = 0, size = sessions.size(); i < size; i++) {
				Session session = sessions.get(i);

				if (source.equals(session)) {
					current = i;
					firePropertyChange("currentSession", null, current);
					break;
				}
			}
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}
	}

	public void windowClosed(WindowEvent event)
	{
		loggerSvc.debug(getClass(), event.toString());

		try {
			JFrame source = (JFrame) event.getWindow();
			if (source.equals(owner)) {
				return;
			}

			Session session = sessions.remove((int) current);
			session.quit();

			int size = sessions.size();
			if (size == 0) {
				current = -1;
				firePropertyChange("sessionCount", null, size);
				return;
			}

			if (current == size) {
				current--;
			}

			firePropertyChange("sessionCount", null, size);

			session = sessions.get(current);
			if (session.isIconified()) {
				shiftSelection(1);
			}
			else {
				session.toFront();
			}
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}
	}

	public void windowClosing(WindowEvent event)
	{
		loggerSvc.debug(getClass(), event.toString());

		try {
			JFrame source = (JFrame) event.getWindow();
			if (source.equals(owner)) {
				return;
			}

			int i, size;
			for (i = 0, size = sessions.size(); i < size; i++) {
				if (source.equals(sessions.get(i))) {
					break;
				}
			}

			if (i == size) {
				return;
			}

			Session session = sessions.remove(i);
			session.removeWindowListener(this);
			session.quit();
			session.dispose();

			size--;
			if (size == 0) {
				current = -1;
				firePropertyChange("sessionCount", null, size);
				return;
			}

			if (i < current || current == size) {
				current--;
			}

			firePropertyChange("sessionCount", null, size);

			session = sessions.get(current);
			if (session.isIconified()) {
				shiftSelection(1);
			}
			else {
				session.toFront();
			}
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}
	}

	public void windowDeactivated(WindowEvent event)
	{
		loggerSvc.debug(getClass(), event.toString());
	}

	public void windowDeiconified(WindowEvent event)
	{
		loggerSvc.debug(getClass(), event.toString());

		try {
			JFrame source = (JFrame) event.getWindow();
			if (source.equals(owner)) {
				setIconified(false);
			}
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}
	}

	public void windowIconified(WindowEvent event)
	{
		loggerSvc.debug(getClass(), event.toString());

		try {
			JFrame source = (JFrame) event.getWindow();
			if (source.equals(owner)) {
				setIconified(true);
			}
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}
	}

	public void windowOpened(WindowEvent event)
	{
		loggerSvc.debug(getClass(), event.toString());

		try {
			JFrame source = (JFrame) event.getWindow();
			if (source.equals(owner)) {
				return;
			}

			firePropertyChange("sessionCount", null, sessions.size());
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}
	}
}
