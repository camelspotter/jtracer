package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.component.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.registry.RegistryService;
import net.libcsdbg.jtracer.service.utility.UtilityService;
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

	protected JToolBar tools;

	protected JTabbedPane tabs;

	protected TraceBar details;


	protected List<TracePane> traces;

	protected Socket connection;

	protected BufferedReader receiver;

	protected Thread server;

	protected Boolean locked;

	protected Boolean active;


	public Session()
	{
		super(Config.initialTitle);
		selfInject();

		this.active = false;
	}

	public Session(JFrame owner, Socket connection)
	{
		super(Config.initialTitle);
		selfInject();

		this.owner = owner;
		this.connection = connection;
		this.locked = false;
		this.active = false;

		setIconImages(utilitySvc.getProjectIcons());

		try {
			connection.shutdownOutput();
			receiver = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String param = registrySvc.get("timeout");
			if (param != null) {
				connection.setSoTimeout(Integer.parseInt(param));
			}
		}
		catch (RuntimeException err) {
			throw err;
		}
		catch (Throwable err) {
			throw new RuntimeException(err);
		}

		tools = new JToolBar();
		tools.add(createTool("Remove trace", (ActionListener) owner));
		tools.add(createTool("Lock", this));
		tools.addSeparator();

		tools.add(createTool("Select previous", (ActionListener) owner));
		tools.add(createTool("Select next", (ActionListener) owner));
		tools.add(createTool("Close", (ActionListener) owner));
		tools.addSeparator();

		tools.add(createTool("Find", this));
		add(tools, BorderLayout.NORTH);

		Button tool = (Button) tools.getComponent(0);
		tool.addActionListener(this);

		traces = new ArrayList<>();
		tabs = new JTabbedPane();
		tabs.addChangeListener(this);
		tabs.setFont(componentSvc.getFont("component"));
		tabs.setForeground(componentSvc.getForegroundColor("component"));
		add(tabs, BorderLayout.CENTER);

		details = new TraceBar();
		add(details, BorderLayout.SOUTH);

		setSize(componentSvc.getDimension("trace"));
		addPropertyChangeListener("sessionRequest", (PropertyChangeListener) owner);
		addPropertyChangeListener("traceCount", (PropertyChangeListener) owner);

		server = new Thread(this, Config.threadName);
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

		try {
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

				tools.getComponent(0).setEnabled(false);
				tools.getComponent(7).setEnabled(false);
			}

			else if (cmd.equals("Lock")) {
				locked = !locked;
				Button tool = (Button) tools.getComponent(1);

				if (locked) {
					tool.setIcon(utilitySvc.loadIcon("unlock24.png"));
					tool.setToolTipText("Unlock");
				}
				else {
					tool.setIcon(utilitySvc.loadIcon("lock24.png"));
					tool.setToolTipText("Lock");
				}
			}

			else if (cmd.equals("Find")) {
				Alert.error(this, "Not implemented yet", false);
			}
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}
	}

	@SuppressWarnings("all")
	public Session addTrace(Map<String, String> request)
	{
		TracePane pane = new TracePane(request);
		traces.add(pane);

		String token = request.get("tstamp");
		details.setTimestamp(Long.parseLong(token, 16));

		token = request.get("exception");
		if (token != null) {
			details.setMessage(token);
		}
		else {
			details.setMessage(Config.defaultStatusMessage);
		}

		InetAddress address = connection.getInetAddress();
		int port = connection.getPort();
		details.setAddress(address.getCanonicalHostName(), port);

		pane.append(request.get("trace"));
		JPanel view = new JPanel(new BorderLayout());
		view.add(pane);
		pane.setMargin(new Insets(6, 2, 0, 2));

		JScrollPane viewport = new JScrollPane(view);
		viewport.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		tabs.addTab("Thread " + request.get("tid"), utilitySvc.loadIcon("new.png"), viewport);

		String path = request.get("path");
		int pid = Integer.parseInt(request.get("pid"), 16);
		String ip = address.getHostAddress();
		setTitle(path + " (" + pid + "@" + ip + ")");

		int length = request.get("request").length();
		StringBuilder message = new StringBuilder(Config.preallocSize);
		message.append("Read ")
		       .append(length)
		       .append(" bytes ")

		       .append("for ")
		       .append(path)
		       .append(" (")
		       .append(pid)
		       .append("@")

		       .append(ip)
		       .append(":")
		       .append(port)
		       .append(")");

		firePropertyChange("sessionRequest", null, message.toString());
		firePropertyChange("traceCount", null, tabs.getTabCount());

		tools.getComponent(0).setEnabled(true);
		tools.getComponent(7).setEnabled(true);

		return this;
	}

	protected Button createTool(String command, ActionListener handler)
	{
		String icon = command.toLowerCase().replace(' ', '_') + "24.png";

		Button retval = new Button(icon, command, handler);
		retval.setMargin(new Insets(2, 2, 2, 2));

		return retval;
	}

	public Map<String, String> getRequest() throws IOException
	{
		int count = 0;
		boolean text = false;
		StringBuilder buffer = new StringBuilder(Config.preallocSize);
		StringBuilder trace = new StringBuilder(Config.preallocSize);

		/* Read a line at a time and parse it */
		Map<String, String> retval = new HashMap<>(Config.requestPreallocSize);
		do {
			String line = receiver.readLine();
			if (line == null) {
				throw new IOException("The peer disconnected prematurely");
			}

			count++;
			buffer.append(line)
			      .append("\n");

			if (line.length() == 0) {
				text = true;
				continue;
			}

			if (text) {
				trace.append(line)
				     .append("\n");

				continue;
			}

			String[] header = line.split(":");
			if (header.length < 2 || header[0].trim().length() == 0) {
				throw new ProtocolException("LDP request format error (" + count + ": " + line + ")");
			}

			StringBuilder field = new StringBuilder(header[1]);
			for (int i = 2; i < header.length; i++) {
				field.append(":")
				     .append(header[i]);
			}

			retval.put(header[0].trim(), field.toString().trim());
		}
		while (!buffer.toString().endsWith("}\n\n"));

		retval.put("trace", trace.toString().trim());
		retval.put("request", buffer.toString().trim());
		return retval;
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
			connection.close();
			receiver.close();
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
				addTrace(getRequest());
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

		try {
			int i = tabs.getSelectedIndex();
			if (i < 0) {
				details.clear();
				return;
			}

			tabs.setIconAt(i, utilitySvc.loadIcon("void.png"));
			TracePane pane = traces.get(i);

			String field = pane.getField("exception");
			if (field != null) {
				details.setMessage(field);
			}
			else {
				details.setMessage(Config.defaultStatusMessage);
			}

			field = pane.getField("tstamp");
			details.setTimestamp(Long.parseLong(field, 16));
			details.setAddress(connection.getInetAddress().getCanonicalHostName(), connection.getPort());
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}
	}


	public static class Config
	{
		public static String initialTitle = "Session";

		public static String threadName = "LDP Service Thread";

		public static String defaultStatusMessage = "Thread stack trace";

		public static Integer preallocSize = 256;

		public static Integer requestPreallocSize = 16;
	}
}
