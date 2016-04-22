package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.annotation.Note;
import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session extends JFrame implements ActionListener,
                                               ChangeListener,
                                               Runnable,
                                               AutoInjectable
{
	private static final long serialVersionUID = 535946441402056043L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;

	@Service
	protected UtilityService utilitySvc;


	protected JFrame owner;

	protected TraceStatusBar status;

	protected JTabbedPane tabs;

	protected TraceToolBar tools;

	protected List<TracePane> traces;


	protected Boolean active;

	protected Socket connection;

	protected Boolean locked;

	protected BufferedReader receiver;

	protected Thread server;


	public Session()
	{
		this(null, null);
	}

	public Session(JFrame owner, Socket connection)
	{
		super(Config.initialTitle);
		selfInject();

		this.owner = owner;
		this.connection = connection;
		active = locked = false;
		traces = new ArrayList<>();

		/* Setup the UI */
		status = new TraceStatusBar();
		add(status, BorderLayout.SOUTH);

		tabs = new JTabbedPane();
		tabs.setFont(componentSvc.getFont("component"));
		tabs.setForeground(componentSvc.getForegroundColor("component"));
		tabs.addChangeListener(this);
		add(tabs, BorderLayout.CENTER);

		tools = new TraceToolBar(this, (ActionListener) owner);
		add(tools, BorderLayout.NORTH);

		setIconImages(utilitySvc.getProjectIcons());
		setSize(componentSvc.getDimension("trace"));

		/* Setup (and fire initial) property change events */
		addPropertyChangeListener("sessionRequest", (PropertyChangeListener) owner);
		addPropertyChangeListener("traceCount", (PropertyChangeListener) owner);
		addPropertyChangeListener("hasTraces", tools);
		addPropertyChangeListener("isLocked", tools);

		firePropertyChange("hasTraces", null, false);
		firePropertyChange("isLocked", null, false);

		/* Setup the network I/O or rollback */
		boolean rollback = false;
		InputStreamReader socketReader = null;
		try {
			connection.shutdownOutput();

			String param = registrySvc.get("network-io-timeout");
			if (param != null) {
				connection.setSoTimeout(Integer.parseInt(param));
			}

			socketReader = new InputStreamReader(connection.getInputStream());
			receiver = new BufferedReader(socketReader);
		}
		catch (RuntimeException err) {
			rollback = true;
			throw err;
		}
		catch (Throwable err) {
			rollback = true;
			throw new RuntimeException(err);
		}
		finally {
			if (rollback) {
				try {
					if (socketReader != null) {
						socketReader.close();
					}

					connection.close();
				}
				catch (Throwable err) {
					loggerSvc.warning(getClass(), err.getMessage());
				}
			}
		}

		/* Start service thread */
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		server = new Thread(group, this, Config.serviceThreadName);
		server.start();

		synchronized (this) {
			while (!active) {
				try {
					wait();
				}
				catch (InterruptedException ignored) {
				}
			}
		}
	}

	@SuppressWarnings("all")
	@Override
	public void actionPerformed(ActionEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
		String cmd = event.getActionCommand();

		if (cmd.equals("Remove trace")) {
			int i = tabs.getSelectedIndex();
			if (i < 0) {
				return;
			}

			traces.remove(i);
			tabs.remove(i);

			int count = tabs.getTabCount();
			firePropertyChange("traceCount", null, count);
			if (count != 0) {
				return;
			}

			if (!locked) {
				owner.toFront();
				dispose();
				return;
			}

			firePropertyChange("hasTraces", null, false);
		}

		else if (cmd.equals("Lock")) {
			locked = !locked;
			firePropertyChange("isLocked", null, locked);
		}

		else if (cmd.equals("Find")) {
			Alert.error(this, "Not implemented yet", false);
		}
	}

	@SuppressWarnings("all")
	public Session addTrace(Map<String, String> request)
	{
		TracePane pane = new TracePane(request);
		pane.append(request.get("trace"));
		pane.setMargin(componentSvc.getInsets("trace"));
		traces.add(pane);

		/* Setup the trace tab viewport */
		JPanel view = new JPanel(new BorderLayout());
		view.add(pane, BorderLayout.CENTER);
		JScrollPane viewport = new JScrollPane(view);
		viewport.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		tabs.addTab("Thread " + request.get("tid").toLowerCase(), utilitySvc.loadIcon("new.png"), viewport);

		/* Set status bar fields */
		status.setTimestamp(Long.parseLong(request.get("tstamp"), 16));

		String header = request.get("exception");
		if (header != null) {
			status.setMessage(header);
		}
		else {
			status.setMessage(Config.defaultStatusMessage);
		}

		/* Set once, only over n/a */
		InetAddress address = connection.getInetAddress();
		int port = connection.getPort();
		status.setAddress(address.getCanonicalHostName(), port);

		/* Customize session frame title (set once, only over n/a) */
		String ip = address.getHostAddress();
		String path = request.get("path");
		String pid = request.get("pid");
		setTitle(path + " (" + pid + "@" + ip + ")");

		/* Logged message */
		StringBuilder message = new StringBuilder(Config.preallocSize);
		message.append("Read ")
		       .append(request.get("request").length())
		       .append(" bytes for ")

		       .append(path)
		       .append(" (")
		       .append(pid)
		       .append("@")

		       .append(ip)
		       .append(":")
		       .append(port)
		       .append(")");

		/* Notify listeners */
		firePropertyChange("sessionRequest", null, message.toString());
		firePropertyChange("traceCount", null, tabs.getTabCount());
		firePropertyChange("hasTraces", null, true);

		return this;
	}

	@Note("Protocol implementation")
	public Map<String, String> getRequest() throws IOException
	{
		Map<String, String> retval = new HashMap<>(Config.requestPreallocSize);

		int count = 0;
		boolean isBody = false;

		/* Read a line at a time and parse it */
		StringBuilder buffer = new StringBuilder(Config.preallocSize);
		StringBuilder trace = new StringBuilder(Config.preallocSize);
		do {
			String line = receiver.readLine();
			if (line == null) {
				if (count != 0) {
					throw new IOException("The peer disconnected prematurely");
				}

				return null;
			}

			count++;
			buffer.append(line)
			      .append("\n");

			if (line.length() == 0) {
				isBody = true;
				continue;
			}

			if (isBody) {
				trace.append(line)
				     .append("\n");

				continue;
			}

			int mark = line.indexOf(":");
			if (mark <= 0) {
				throw new ProtocolException("LDP request format error (" + count + ": " + line + ")");
			}

			String header = line.substring(0, mark).trim();
			String field = line.substring(mark + 1).trim();
			if (header.length() == 0 || field.length() == 0) {
				throw new ProtocolException("LDP request format error (" + count + ": " + line + ")");
			}

			retval.put(header, field);
		}
		while (!buffer.toString().endsWith("}\n\n"));

		retval.put("trace", trace.toString().trim());
		retval.put("request", buffer.toString().trim());

		return validateRequest(retval);
	}

	public Integer getTraceCount()
	{
		return tabs.getTabCount();
	}

	public Boolean isIconified()
	{
		return (getExtendedState() & ICONIFIED) != 0;
	}

	public Session quit()
	{
		try {
			if (receiver != null) {
				receiver.close();
			}

			if (connection != null) {
				connection.close();
			}

			receiver = null;
			connection = null;
		}
		catch (Throwable ignored) {
		}

		return this;
	}

	@Override
	public void run()
	{
		synchronized (this) {
			active = true;
			notifyAll();
		}

		/* Listen for incoming connections */
		while (server == Thread.currentThread()) {
			try {
				Map<String, String> request = getRequest();

				/* Indication the peer keep-alive socket has closed, session goes inactive */
				if (request == null) {
					quit();
					server = null;
					continue;
				}

				addTrace(request);
			}
			catch (Throwable err) {
				loggerSvc.catching(getClass(), err);
				server = null;
			}
		}

		active = false;
	}

	public Integer setIconified(Boolean iconified)
	{
		int state = getExtendedState();
		if (iconified) {
			state |= ICONIFIED;
		}
		else {
			state &= ~ICONIFIED;
		}

		setExtendedState(state);
		return state;
	}

	@Override
	public void stateChanged(ChangeEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		int i = tabs.getSelectedIndex();
		if (i < 0) {
			status.clear();
			return;
		}

		tabs.setIconAt(i, utilitySvc.loadIcon("void.png"));
		TracePane pane = traces.get(i);

		String field = pane.getRequestSection("exception");
		if (field != null) {
			status.setMessage(field);
		}
		else {
			status.setMessage(Config.defaultStatusMessage);
		}

		status.setTimestamp(Long.parseLong(pane.getRequestSection("tstamp"), 16));
		status.setAddress(connection.getInetAddress().getCanonicalHostName(), connection.getPort());
	}

	protected Map<String, String> validateRequest(Map<String, String> request)
	{
		/* Search mandatory headers */
		for (String header : Config.mandatoryRequestHeaders) {
			String value = request.get(header);

			if (value == null || value.length() == 0) {
				throw new IllegalStateException("Request doesn't contain a mandatory header (" + header + ")");
			}
		}

		for (String header : request.keySet()) {
			String value = request.get(header);

			if (value == null || value.length() == 0) {
				request.remove(header);
			}
		}

		return request;
	}

	public static class Config
	{
		public static Integer preallocSize = 256;

		public static Integer requestPreallocSize = 16;


		public static String initialTitle = "Session";

		public static String defaultStatusMessage = "Thread stack trace";

		public static String serviceThreadName = "LDP Service Thread";


		public static String[] mandatoryRequestHeaders = { "path", "pid", "request", "tid", "trace", "tstamp" };
	}
}
