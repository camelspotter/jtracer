package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.component.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.registry.RegistryService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TraceBar extends JPanel implements AutoInjectable
{
	private static final long serialVersionUID = 8901737228941794556L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;


	protected Map<String, JLabel> fields;


	public TraceBar()
	{
		super();
		selfInject();

		fields = new HashMap<>();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints bagConstraints = new GridBagConstraints();
		setLayout(layout);

		JPanel field = createField("message");
		bagConstraints.gridx = 0;
		bagConstraints.gridy = 0;
		bagConstraints.weightx = 1;
		bagConstraints.insets = new Insets(0, 0, 0, 0);
		bagConstraints.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(field, bagConstraints);
		add(field);

		field = createField("timestamp");
		bagConstraints.gridx++;
		bagConstraints.weightx = 0;
		bagConstraints.fill = GridBagConstraints.NONE;
		layout.setConstraints(field, bagConstraints);
		add(field);

		field = createField("address");
		bagConstraints.gridx++;
		layout.setConstraints(field, bagConstraints);
		add(field);
	}

	public TraceBar clear()
	{
		fields.get("message")
		      .setText("n/a");

		fields.get("timestamp")
		      .setText("n/a");

		fields.get("address")
		      .setText("n/a");

		return this;
	}

	protected JPanel createField(String name)
	{
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints bagConstraints = new GridBagConstraints();
		JPanel retval = new JPanel(layout);

		retval.setBackground(componentSvc.getBackgroundColor("status"));
		retval.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		JLabel field = new JLabel("n/a");
		field.setFont(componentSvc.getFont("status"));
		field.setForeground(componentSvc.getForegroundColor("status"));

		if (name.equals("message")) {
			bagConstraints.weightx = 1;
			bagConstraints.anchor = GridBagConstraints.EAST;
		}

		bagConstraints.insets = new Insets(1, 8, 1, 8);
		layout.setConstraints(field, bagConstraints);
		retval.add(field);

		fields.put(name, field);
		return retval;
	}

	public TraceBar setAddress(String address, int port)
	{
		fields.get("address")
		      .setText(address + ":" + port);

		return this;
	}

	public TraceBar setField(String name, String text)
	{
		fields.get(name)
		      .setText(text);

		return this;
	}

	public TraceBar setMessage(String message)
	{
		fields.get("message")
		      .setText(message);

		return this;
	}

	/* todo Use locales to output dates */
	public TraceBar setTimestamp(Long timestamp)
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timestamp / 1000);

		int d1 = c.get(Calendar.DATE);
		int d2 = c.get(Calendar.MONTH) + 1;
		int d3 = c.get(Calendar.YEAR);

		StringBuilder text = new StringBuilder();
		text.append((d1 < 10) ? "0" : "")
		    .append(d1)
		    .append("/")

		    .append((d2 < 10) ? "0" : "")
		    .append(d2).append("/")
		    .append(d3)
		    .append(" ");

		int h1 = c.get(Calendar.HOUR);
		int h2 = c.get(Calendar.MINUTE);
		int h3 = c.get(Calendar.SECOND);

		text.append((h1 < 10) ? "0" : "")
		    .append(h1)
		    .append(":")

		    .append((h2 < 10) ? "0" : "")
		    .append(h2)
		    .append(":")

		    .append((h3 < 10) ? "0" : "")
		    .append(h3);

		fields.get("timestamp")
		      .setText(text.toString());

		return this;
	}
}
