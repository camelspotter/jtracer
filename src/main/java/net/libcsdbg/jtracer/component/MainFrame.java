package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.ApplicationCore;
import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.core.GenericUncaughtExceptionHandler;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.*;

public class MainFrame extends JFrame implements ActionListener,
                                                 PropertyChangeListener,
                                                 Runnable,
                                                 AutoInjectable
{
	private static final long serialVersionUID = 1201750874107334406L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;

	@Service
	protected UtilityService utilitySvc;


	protected AboutDialog about;

	protected SessionManager desktop;

	protected LogPane log;

	protected MenuBar menu;

	protected StatusBar status;

	protected ToolBar tools;


	protected Thread daemon;

	protected Boolean daemonActive;

	protected ServerSocket listener;


	public MainFrame()
	{
		super();
		selfInject();

		String fullName = registrySvc.get("full-name");
		setTitle(fullName);
		setIconImages(utilitySvc.getProjectIcons());

		daemonActive = false;
		menu = new MenuBar(this);
		tools = new ToolBar(this);
		log = new LogPane();
		status = new StatusBar(fullName + " initialized");

		desktop = new SessionManager(this);
		addWindowListener(desktop);

		setJMenuBar(menu);
		add(tools, BorderLayout.NORTH);
		addViewport();
		add(status, BorderLayout.SOUTH);

		addPropertyChangeListener("isServing", menu);
		addPropertyChangeListener("isServing", tools);
		addPropertyChangeListener("hasClients", menu);
		addPropertyChangeListener("hasClients", tools);

		firePropertyChange("isServing", null, false);
		firePropertyChange("hasClients", null, false);

		log.appendln(fullName + " initialized", "status", true)
		   .appendln("Ready to start serving", "status", true);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setSize(componentSvc.getDimension("initial"));
		setLocationRelativeTo(null);
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());
		String cmd = event.getActionCommand();

		/* Service menu commands */
		if (cmd.equals("Start")) {
			startService();
		}

		else if (cmd.equals("Stop")) {
			stopService();
		}

		else if (cmd.equals("Restart")) {
			log.appendln("Restarting server (reload configuration)...", "status");
			stopService();
			startService();
		}

		else if (cmd.equals("Clear log")) {
			log.clear();
		}

		else if (cmd.equals("Quit")) {
			if (Alert.prompt(this, ApplicationCore.Config.quitPrompt)) {
				dispose();
			}
		}

		/* View menu commands */
		else if (cmd.equals("Toolbar")) {
			if (menu.getToggleState(0)) {
				add(tools, BorderLayout.NORTH);
			}
			else {
				remove(tools);
			}

			validate();
		}

		else if (cmd.equals("Statusbar")) {
			if (menu.getToggleState(1)) {
				add(status, BorderLayout.SOUTH);
			}
			else {
				remove(status);
			}

			validate();
		}

		else if (cmd.equals("Always on top")) {
			boolean state = menu.getToggleState(2);
			setAlwaysOnTop(state);
			desktop.setAlwaysOnTop(-1, state);
		}

		else if (cmd.equals("Find")) {
			Alert.error(this, "Not implemented yet", false);
		}

		else if (cmd.equals("Preferences")) {
			Alert.error(this, "Not implemented yet", false);
		}

		else if (cmd.equals("Current log level")) {
			new InputPrompt(this, cmd);
			logCurrentLogLevel();
		}

		else if (cmd.equals("Lower log level")) {
			if (loggerSvc.logLevelDown()) {
				logCurrentLogLevel();
			}
		}

		else if (cmd.equals("Higher log level")) {
			if (loggerSvc.logLevelUp()) {
				logCurrentLogLevel();
			}
		}

		else if (cmd.equals("Full screen")) {
			GraphicsDevice device =
				GraphicsEnvironment.getLocalGraphicsEnvironment()
				                   .getDefaultScreenDevice();

			if (!device.isFullScreenSupported()) {
				String message = "Graphics device '" + device.getIDstring() + "' doesn't support full screen windows";
				log.appendln(message, "alert");
				loggerSvc.warning(getClass(), message);
				return;
			}

			JFrame current = (JFrame) device.getFullScreenWindow();
			if (current == null) {
				device.setFullScreenWindow(this);
			}
			else {
				device.setFullScreenWindow(null);
			}
		}

		/* Client menu commands */
		else if (cmd.equals("Select previous")) {
			desktop.shiftSelection(-1);
		}

		else if (cmd.equals("Select next")) {
			desktop.shiftSelection(1);
		}

		else if (cmd.equals("Close")) {
			/* This fixes a very tricky bug induced by the OS window manager */
			toFront();
			desktop.disposeCurrent();
		}

		else if (cmd.equals("Cascade")) {
			desktop.cascade();
		}

		else if (cmd.equals("Minimize all")) {
			desktop.setIconified(true);
		}

		else if (cmd.equals("Restore all")) {
			desktop.setIconified(false);
		}

		else if (cmd.equals("Close all")) {
			if (Alert.prompt(this, "Close all client windows?")) {
				desktop.disposeAll();
			}
		}

		else if (cmd.startsWith("Select session")) {
			String[] parts = cmd.split("\\s");
			desktop.setCurrent(Integer.valueOf(parts[2]));
		}

		/* Help menu commands */
		else if (cmd.equals("Online documentation") ||
		         cmd.equals("Bug tracker") ||
		         cmd.equals("Submit feedback")) {
			String key = "url-" + cmd.toLowerCase().replace(' ', '-');
			String url = registrySvc.get(key);

			if (url == null) {
				String message = "URL for key '" + key + "' not registered";
				log.appendln(message, "error");
				loggerSvc.error(getClass(), message);
				return;
			}

			try {
				utilitySvc.browse(new URL(url));
			}
			catch (MalformedURLException err) {
				throw new RuntimeException(err);
			}
		}

		else if (cmd.equals("Check for updates")) {
			Alert.error(this, "Not implemented yet", false);
		}

		else if (cmd.startsWith("About")) {
			if (about == null) {
				about = new AboutDialog(this);
			}

			about.setLocationRelativeTo(this);
			about.setVisible(true);
		}
	}

	protected MainFrame addViewport()
	{
		JPanel nowrap = new JPanel(new BorderLayout());
		nowrap.add(log, BorderLayout.CENTER);

		JScrollPane viewport = new JScrollPane(nowrap);
		viewport.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints bagConstraints = new GridBagConstraints();
		JPanel inset = new JPanel(layout);

		bagConstraints.gridy = 0;
		bagConstraints.gridx = 0;
		bagConstraints.weightx = 1;
		bagConstraints.weighty = 1;
		bagConstraints.fill = GridBagConstraints.BOTH;
		bagConstraints.insets = new Insets(0, 2, 0, 1);

		layout.setConstraints(viewport, bagConstraints);
		inset.add(viewport);
		add(inset, BorderLayout.CENTER);

		return this;
	}

	public MainFrame logCurrentLogLevel()
	{
		String level =
			loggerSvc.dynamicLogLevel()
			         .get()
			         .name();

		log.appendln("The current log level is " + level, "alert");
		return this;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		String key = event.getPropertyName();
		Object value = event.getNewValue();

		switch (key) {
		case "sessionCount":
			firePropertyChange("hasClients", null, (Integer) value != 0);

		case "traceCount":
			menu.listSessions(desktop.getSessions(), desktop.getCurrent());
			status.setIndicators(desktop.getSessionCount(), desktop.getTraceCount());
			break;

		case "currentSession":
			menu.setSelectedSession((Integer) value);
			break;

		case "sessionRequest":
			log.appendln((String) value, "data");
		}
	}

	@SuppressWarnings("LoopStatementThatDoesntLoop")
	@Override
	public void run()
	{
		synchronized (this) {
			daemonActive = true;
			notifyAll();
		}

		try {
			/* Create the listener socket unbound */
			listener = new ServerSocket();
			listener.setReuseAddress(true);

			InetSocketAddress binding = new InetSocketAddress(Config.defaultPort);
			while (true) {
				String param = registrySvc.get("address");
				if (param == null) {
					break;
				}

				String[] parts = param.split(",");
				if (parts.length != 2) {
					break;
				}

				String ip = parts[0].trim();
				if (!ip.equals("*") && !ip.matches(Config.ipPattern)) {
					break;
				}

				String portParam = parts[1].trim();
				if (!portParam.matches(Config.portPattern)) {
					break;
				}

				int port = Integer.parseInt(portParam);
				if (ip.equals("*")) {
					binding = new InetSocketAddress(port);
				}
				else {
					binding = new InetSocketAddress(ip, port);
				}

				break;
			}

			listener.bind(binding);

			InetAddress address = binding.getAddress();
			StringBuilder message = new StringBuilder("Accepting connections @");
			message.append(address.getHostAddress())
			       .append(":")
			       .append(binding.getPort());

			if (address.isAnyLocalAddress()) {
				message.append(" (all local)");
			}
			else {
				message.append(" (")
				       .append(address.getCanonicalHostName())
				       .append(")");
			}

			log.appendln(message.toString(), "status");
			firePropertyChange("isServing", null, true);
			status.startUptimeTimer();

			/* Listen for incoming connections */
			while (daemon == Thread.currentThread()) {
				status.setMessage("Waiting for connections", true);

				try {
					Socket peer = listener.accept();

					String peerAddress =
						peer.getInetAddress()
						    .getHostAddress();

					log.appendln("Connected peer @" + peerAddress + ":" + peer.getPort(), "status");
					desktop.register(peer);
				}
				catch (Throwable err) {
					if (err instanceof SocketException) {
						log.appendln("Listener socket session terminated", "error");
					}
					else {
						log.appendln(err.getMessage(), "error");
						loggerSvc.catching(getClass(), err);
					}
				}
			}

			status.stopUptimeTimer();
			status.setMessage("Service complete", true);
			firePropertyChange("isServing", null, false);
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);

			getToolkit().beep();
			shutdown();

			log.appendln("Failed to start server (" + err.toString() + ")", "error");
			status.setMessage("Server failed", false);
		}

		daemonActive = false;
	}

	public MainFrame shutdown()
	{
		try {
			daemon = null;

			if (listener != null) {
				listener.close();
				listener = null;
			}
		}
		catch (Throwable ignored) {
		}

		return this;
	}

	public synchronized MainFrame startService()
	{
		if (daemon != null) {
			return this;
		}

		log.appendln("Starting server...", "status");

		daemon = new Thread(Thread.currentThread().getThreadGroup(), this, Config.daemonName);
		daemon.setUncaughtExceptionHandler(new GenericUncaughtExceptionHandler());
		daemon.setDaemon(true);
		daemon.start();

		while (!daemonActive) {
			try {
				wait();
			}
			catch (InterruptedException ignored) {
			}
		}

		return this;
	}

	public synchronized MainFrame stopService()
	{
		if (daemon == null) {
			return this;
		}

		log.appendln("Stopping server...", "status");
		Thread handle = daemon;
		shutdown();

		try {
			if (handle.isAlive()) {
				handle.join();
			}
		}
		catch (InterruptedException ignored) {
		}

		log.appendln("Service stopped. All incoming connections dropped", "status");
		return this;
	}


	public static class Config
	{
		public static Integer defaultPort = 4242;


		public static String daemonName = "LDP Service Thread";

		public static String ipPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$";

		public static String portPattern = "^[0-9]{1,5}$";
	}
}
