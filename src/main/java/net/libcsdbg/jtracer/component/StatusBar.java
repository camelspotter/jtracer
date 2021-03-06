package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.component.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.registry.RegistryService;
import net.libcsdbg.jtracer.service.utility.UtilityService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class StatusBar extends JPanel implements ActionListener, AutoInjectable
{
	private static final long serialVersionUID = 159085360087933034L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;

	@Service
	protected UtilityService utilitySvc;


	protected Long uptime;

	protected Timer timer;

	protected Map<String, JLabel> fields;


	public StatusBar()
	{
		super();
		selfInject();
	}


	public StatusBar(String message)
	{
		super();
		selfInject();

		uptime = 0L;
		fields = new HashMap<>();

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints bagConstraints = new GridBagConstraints();
		setLayout(layout);

		JPanel field = createField("status", message);
		bagConstraints.gridx = 0;
		bagConstraints.gridy = 0;
		bagConstraints.weightx = 1;
		bagConstraints.insets = new Insets(0, 0, 0, 0);
		bagConstraints.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(field, bagConstraints);
		add(field);

		field = createField("uptime", "Uptime 00:00:00");
		bagConstraints.gridx++;
		bagConstraints.weightx = 0;
		bagConstraints.fill = GridBagConstraints.NONE;
		layout.setConstraints(field, bagConstraints);
		add(field);

		field = createField("protocol", "TCP");
		bagConstraints.gridx++;
		layout.setConstraints(field, bagConstraints);
		add(field);

		field = createField("sessionCount", "0 sessions");
		bagConstraints.gridx++;
		layout.setConstraints(field, bagConstraints);
		add(field);

		field = createField("traceCount", "0 traces");
		bagConstraints.gridx++;
		layout.setConstraints(field, bagConstraints);
		add(field);
	}


	@Override
	public void actionPerformed(ActionEvent event)
	{
		try {
			uptime++;
			renderUptime();
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}
	}


	protected JPanel createField(String tag, String text)
	{
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints bagConstraints = new GridBagConstraints();
		JPanel retval = new JPanel(layout);

		retval.setBackground(componentSvc.getBackgroundColor("status"));
		retval.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		JLabel field = new JLabel(text);
		field.setFont(componentSvc.getFont("status"));
		field.setForeground(componentSvc.getForegroundColor("status"));

		if (tag.equals("status")) {
			field.setIcon(utilitySvc.loadIcon("stat_ok.png"));
			field.setIconTextGap(6);
			bagConstraints.weightx = 1;
			bagConstraints.anchor = GridBagConstraints.EAST;
		}

		bagConstraints.insets = new Insets(1, 8, 1, 8);
		layout.setConstraints(field, bagConstraints);
		retval.add(field);

		fields.put(tag, field);
		return retval;
	}


	protected StatusBar renderUptime()
	{
		long now = uptime;
		long hours = now / 3600;

		now = now % 3600;
		long minutes = now / 60;
		long seconds = now % 60;

		String tm = "Uptime ";
		tm += ((hours < 10) ? "0" : "") + hours + ":";
		tm += ((minutes < 10) ? "0" : "") + minutes + ":";
		tm += ((seconds < 10) ? "0" : "") + seconds;

		fields.get("uptime").setText(tm);
		return this;
	}


	public StatusBar setIndicator(String name, Integer count)
	{
		JLabel field = fields.get(name + "Count");

		String text = count + " " + name;
		if (count != 1) {
			text += "s";
		}

		field.setText(text);

		String key = (count > 0) ? "status-active-counter" : "status";
		field.getParent()
		     .setBackground(componentSvc.getBackgroundColor(key));

		return this;
	}


	public StatusBar setIndicators(Integer sessions, Integer traces)
	{
		return setIndicator("session", sessions).setIndicator("trace", traces);
	}


	public StatusBar setMessage(final String message, final Boolean normal)
	{
		/* If the rendering is done by the event dispatching thread */
		if (SwingUtilities.isEventDispatchThread()) {
			JLabel field = fields.get("status");
			field.setText(message);

			if (!normal) {
				field.setForeground(componentSvc.getForegroundColor("status-error"));
				field.setIcon(utilitySvc.loadIcon("stat_err.png"));
			}
			else {
				field.setForeground(componentSvc.getForegroundColor("status"));
				field.setIcon(utilitySvc.loadIcon("stat_ok.png"));
			}

			return this;
		}

		/* Register a Runnable to be called by the event dispatching thread */
		try {
			SwingUtilities.invokeLater(() -> setMessage(message, normal));
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}

		return this;
	}


	public StatusBar startUptimeTimer()
	{
		/* If the rendering is done by the event dispatching thread */
		if (SwingUtilities.isEventDispatchThread()) {
			fields.get("uptime")
			      .getParent()
			      .setBackground(componentSvc.getBackgroundColor("status-active-uptime"));

			timer = new Timer(1000, this);
			timer.setActionCommand("Uptime timer");
			timer.setCoalesce(false);
			timer.start();

			return this;
		}

		/* Register a Runnable to be called by the event dispatching thread */
		try {
			SwingUtilities.invokeLater(this::startUptimeTimer);
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}

		return this;
	}


	public StatusBar stopUptimeTimer(final Boolean reset, final Boolean normal)
	{
		/* If the rendering is done by the event dispatching thread */
		if (SwingUtilities.isEventDispatchThread()) {
			timer.stop();
			timer = null;

			JLabel field = fields.get("uptime");
			field.getParent()
			     .setBackground(componentSvc.getBackgroundColor("status"));

			if (!normal) {
				field.setForeground(componentSvc.getForegroundColor("status-error"));
			}

			if (reset) {
				uptime = 0L;
				renderUptime();
			}

			return this;
		}

		/* Register a Runnable to be called by the event dispatching thread */
		try {
			SwingUtilities.invokeLater(() -> stopUptimeTimer(reset, normal));
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}

		return this;
	}


	public StatusBar stopUptimeTimer()
	{
		return stopUptimeTimer(true, true);
	}
}
