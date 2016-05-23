package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.annotation.Factory;
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

public class Desktop extends Component implements AutoInjectable,
                                                  WindowListener
{
	private static final long serialVersionUID = -560110208869609605L;

	protected static GridPresets gridPresets;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;


	protected Integer current;

	protected JFrame owner;

	protected final List<Session> sessions;


	private Desktop()
	{
		sessions = null;
	}

	public Desktop(JFrame parent)
	{
		super();
		selfInject();

		if (gridPresets == null) {
			gridPresets = componentSvc.getGridPresets("desktop");
		}

		current = -1;
		owner = parent;
		sessions = new ArrayList<>();

		addPropertyChangeListener("sessionCount", (PropertyChangeListener) owner);
		addPropertyChangeListener("currentSession", (PropertyChangeListener) owner);
	}

	public Desktop alignSession(Integer index)
	{
		if (index < 0 || index >= sessions.size()) {
			return this;
		}

		int rowSize = gridPresets.rowSize().get();
		int row = (index + rowSize) / rowSize;
		int column = index % rowSize;

		int step = gridPresets.step().get();
		int x = gridPresets.baseX().get() + step * column;
		int y = gridPresets.baseY().get() + step * (row + column);

		sessions.get(index)
		        .setLocation(x, y);

		return this;
	}

	public Desktop cascade()
	{
		for (int i = sessions.size() - 1; i >= 0; i--) {
			alignSession(i);
		}

		return this;
	}

	public Session closeCurrentSession()
	{
		int size = sessions.size();
		if (current < 0 || current >= size) {
			return null;
		}

		Session retval =
			sessions.remove(current.intValue())
			        .quit();

		retval.setVisible(false);
		firePropertyChange("sessionCount", null, --size);

		if (size == 0) {
			current = -1;
			return retval;
		}
		else if (current == size) {
			current--;
		}

		Session selected = sessions.get(current);
		if (selected.isIconified()) {
			shiftSessionSelection(false);
		}
		else {
			selected.toFront();
		}

		return retval;
	}

	public Desktop disposeAll()
	{
		while (sessions.size() > 0) {
			Session s = sessions.remove(0);
			s.removeWindowListener(this);
			s.quit()
			 .dispose();
		}

		current = -1;
		firePropertyChange("sessionCount", null, 0);

		return this;
	}

	public Session disposeCurrent()
	{
		return disposeSession(current);
	}

	public Session disposeSession(Integer index)
	{
		Session retval = null;
		if (index >= 0 && index < sessions.size()) {
			retval = sessions.get(index);
			retval.dispose();
		}

		return retval;
	}

	public Session getCurrent()
	{
		if (current >= 0 && current < sessions.size()) {
			return sessions.get(current);
		}

		return null;
	}

	public Integer getCurrentIndex()
	{
		return current;
	}

	public Integer getSessionCount()
	{
		return sessions.size();
	}

	public final List<Session> getSessions()
	{
		return sessions;
	}

	public Integer getTraceCount()
	{
		int count = 0;
		for (Session s : sessions) {
			count += s.getTraceCount();
		}

		return count;
	}

	@Factory(Factory.Type.POJO)
	public synchronized Session registerPeer(Socket peer)
	{
		Session retval = new Session(owner, this, peer);
		retval.addWindowListener(this);

		sessions.add(retval);
		current = sessions.size() - 1;

		alignSession(current);
		retval.setVisible(true);
		retval.toFront();
		retval.setAlwaysOnTop(owner.isAlwaysOnTop());

		return retval;
	}

	public Desktop setAlwaysOnTop(Integer index, Boolean alwaysOnTop)
	{
		int size = sessions.size();
		if (index < 0) {
			while (--size >= 0) {
				sessions.get(size)
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

	public Desktop setCurrent(Integer index)
	{
		if (index < 0 || index >= sessions.size()) {
			return this;
		}

		Session s = sessions.get(index);
		s.setIconified(false);
		s.toFront();

		current = index;
		return this;
	}

	@SuppressWarnings("all")
	public Desktop setIconified(final Boolean iconified)
	{
		/* Defer execution to another thread */
		Runnable worker = () -> {
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
		};

		ThreadGroup group =
			Thread.currentThread()
			      .getThreadGroup();

		Thread runner = new Thread(group, worker, Config.iconificationWorkerName);
		runner.setDaemon(true);
		runner.setUncaughtExceptionHandler(ApplicationCore.getCurrentApplicationCore()
		                                                  .getUncaughtExceptionHandler());

		runner.start();
		return this;
	}

	public Desktop shiftSessionSelection(Boolean ascending)
	{
		int step = (ascending) ? -1 : 1;
		int size = sessions.size();
		if (size == 0) {
			return this;
		}

		Session previous = sessions.get(current);
		Session selected;

		/* This loop stops when either a non-iconified window is selected or (if all windows are iconified) when a full circle is completed */
		do {
			current += step;
			if (current < 0) {
				current = size - 1;
			}
			else if (current > size - 1) {
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

		Window source = event.getWindow();
		if (source.equals(owner)) {
			return;
		}

		for (int i = sessions.size() - 1; i >= 0; i--) {
			if (sessions.get(i).equals(source)) {
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

		if (!event.getWindow().equals(owner)) {
			closeCurrentSession();
		}
	}

	@Override
	public void windowClosing(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		if (!event.getWindow().equals(owner)) {
			closeCurrentSession();
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

		if (event.getWindow().equals(owner)) {
			setIconified(false);
		}
	}

	@Override
	public void windowIconified(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		if (event.getWindow().equals(owner)) {
			setIconified(true);
		}
	}

	@Override
	public void windowOpened(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		if (!event.getWindow().equals(owner)) {
			firePropertyChange("sessionCount", null, sessions.size());
		}
	}


	public static class Config
	{
		public static Integer iconificationDelay = 128;

		public static String iconificationWorkerName = "Session Minimizing/Maximizing Worker Thread";
	}
}
