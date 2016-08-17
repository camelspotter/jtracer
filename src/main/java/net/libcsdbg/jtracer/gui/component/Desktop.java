package net.libcsdbg.jtracer.gui.component;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.annotation.Note;
import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.gui.container.Session;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.graphics.value.GridPresets;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

@Note("Synchronization is needed as multiple threads perform desktop operations concurrently")
public class Desktop extends Component implements AutoInjectable,
                                                  WindowListener
{
	private static final long serialVersionUID = -560110208869609605L;

	protected static GridPresets gridPresets = null;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected UtilityService utilitySvc;


	protected Integer current;

	protected JFrame root;

	protected final List<Session> sessions;


	private Desktop()
	{
		sessions = null;
	}

	public Desktop(JFrame rootFrame)
	{
		super();
		selfInject();

		if (gridPresets == null) {
			gridPresets = componentSvc.getGridPresets("desktop");
		}

		current = -1;
		root = rootFrame;
		sessions = new LinkedList<>();

		addPropertyChangeListener("sessionCount", (PropertyChangeListener) root);
		addPropertyChangeListener("currentSession", (PropertyChangeListener) root);
	}

	protected Desktop alignSession(Integer index)
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

	@Note("Currently called only from within event handlers")
	public synchronized Desktop cascadeSessions()
	{
		for (int i = sessions.size() - 1; i >= 0; i--) {
			alignSession(i);
		}

		return this;
	}

	@Note("Currently called only from within event handlers")
	public synchronized Session closeCurrentSession()
	{
		int size = sessions.size();
		if (size == 0) {
			current = -1;
			return null;
		}

		if (current < 0) {
			throw new IllegalStateException("No session currently selected");
		}
		else if (current >= size) {
			throw new IllegalStateException("Current session index out of bounds (" + current + " >= " + size + ")");
		}

		Session retval =
			sessions.remove(current.intValue())
			        .passivate();

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

		retval.removeWindowListener(this);
		retval.dispose();
		return retval;
	}

	@Note("Currently called only from within event handlers")
	public synchronized Desktop closeSessions()
	{
		while (sessions.size() > 0) {
			Session s = sessions.remove(0);
			s.removeWindowListener(this);
			s.passivate()
			 .dispose();
		}

		current = -1;
		firePropertyChange("sessionCount", null, 0);

		return this;
	}

	public Integer getCurrentIndex()
	{
		return current;
	}

	public synchronized Session getCurrentSession()
	{
		if (current >= 0 && current < sessions.size()) {
			return sessions.get(current);
		}

		return null;
	}

	public Integer getSessionCount()
	{
		return sessions.size();
	}

	public final List<Session> getSessions()
	{
		return sessions;
	}

	public synchronized Integer getTraceCount()
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
		Session retval = new Session(root, this, peer);
		retval.addWindowListener(this);

		sessions.add(retval);
		current = sessions.size() - 1;

		alignSession(current);
		retval.setVisible(true);

		retval.toFront();
		retval.setAlwaysOnTop(root.isAlwaysOnTop());

		return retval;
	}

	@Note("Currently called only from within event handlers")
	public synchronized Desktop selectSession(Integer index)
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

	@Note("Currently called only from within event handlers")
	public synchronized Desktop setSessionsAlwaysOnTop(Boolean alwaysOnTop)
	{
		int size = sessions.size();
		while (--size >= 0) {
			sessions.get(size)
			        .setAlwaysOnTop(alwaysOnTop);
		}

		return this;
	}

	@Note("Currently called only from within event handlers")
	public Desktop setSessionsIconified(final Boolean iconified)
	{
		/* Defer execution to another thread, for smooth rendering */
		Runnable worker = () -> {
			synchronized (this) {
				int previous = current;
				for (Session session : sessions) {
					session.setIconified(iconified);

					try {
						Thread.sleep(Config.iconificationDelay);
					}
					catch (InterruptedException ignored) {
					}
				}

				/* When a window is restored, it's activated by the window manager */
				if (!iconified && previous >= 0) {
					sessions.get(previous)
					        .toFront();
				}
			}
		};

		utilitySvc.fork(worker, Config.iconificationWorkerName, true);
		return this;
	}

	@Note("Currently called only from within event handlers")
	public synchronized Desktop shiftSessionSelection(Boolean ascending)
	{
		int size = sessions.size();
		if (size == 0) {
			return this;
		}

		Session previous = sessions.get(current);
		Session selected;

		/* This loop stops when either a non-iconified window is selected or (if all windows are iconified) when a full circle is completed */
		int step = (ascending) ? -1 : 1;
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
		if (source.equals(root)) {
			return;
		}

		synchronized (this) {
			for (int i = sessions.size() - 1; i >= 0; i--) {
				if (sessions.get(i).equals(source)) {
					current = i;
					firePropertyChange("currentSession", null, current);
					break;
				}
			}
		}
	}

	@Override
	public void windowClosed(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
	}

	@Override
	public void windowClosing(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		if (!event.getWindow().equals(root)) {
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

		if (event.getWindow().equals(root)) {
			setSessionsIconified(false);
		}
	}

	@Override
	public void windowIconified(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		if (event.getWindow().equals(root)) {
			setSessionsIconified(true);
		}
	}

	@Override
	public void windowOpened(WindowEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		if (event.getWindow().equals(root)) {
			return;
		}

		synchronized (this) {
			firePropertyChange("sessionCount", null, sessions.size());
		}
	}


	public static class Config
	{
		public static Integer iconificationDelay = 128;

		public static String iconificationWorkerName = "Session Minimizing/Maximizing Worker Thread";
	}
}
