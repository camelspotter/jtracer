package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.annotation.Note;
import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class TraceStatusBar extends JPanel implements AutoInjectable
{
	private static final long serialVersionUID = 8901737228941794556L;


	@Service
	protected ComponentService componentSvc;


	protected Map<String, JLabel> fields;


	public TraceStatusBar()
	{
		super();
		selfInject();
		fields = new HashMap<>(3);

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints bagConstraints = new GridBagConstraints();
		setLayout(layout);

		JPanel field = createField("message");
		bagConstraints.gridx = 0;
		bagConstraints.gridy = 0;
		bagConstraints.weightx = 1;
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

	public TraceStatusBar clear()
	{
		fields.get("message")
		      .setText(Config.undefinedValue);

		fields.get("timestamp")
		      .setText(Config.undefinedValue);

		fields.get("address")
		      .setText(Config.undefinedValue);

		return this;
	}

	@Factory(Factory.Type.POJO)
	protected JPanel createField(String name)
	{
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints bagConstraints = new GridBagConstraints();
		JPanel retval = new JPanel(layout);

		retval.setBackground(componentSvc.getBackgroundColor("status"));
		retval.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		JLabel field = new JLabel(Config.undefinedValue);
		field.setFont(componentSvc.getFont("status"));
		field.setForeground(componentSvc.getForegroundColor("status"));

		if (name.equals("message")) {
			bagConstraints.weightx = 1;
			bagConstraints.anchor = GridBagConstraints.EAST;
		}

		bagConstraints.insets = componentSvc.getInsets("status");
		layout.setConstraints(field, bagConstraints);
		retval.add(field);

		fields.put(name, field);
		return retval;
	}

	public TraceStatusBar setAddress(String address, Integer port)
	{
		return setField("address", address + ":" + port);
	}

	public TraceStatusBar setField(String name, String text)
	{
		fields.get(name)
		      .setText(text);

		return this;
	}

	public TraceStatusBar setMessage(String message)
	{
		return setField("message", message);
	}

	@Note("Timestamp is in microseconds")
	public TraceStatusBar setTimestamp(Long timestamp)
	{
		Instant seconds = Instant.ofEpochMilli(timestamp / 1000);
		ZonedDateTime now = ZonedDateTime.ofInstant(seconds, ZoneId.systemDefault());

		StringBuilder text = new StringBuilder(Config.preallocSize);
		int temporal = now.getDayOfMonth();
		text.append((temporal < 10) ? "0" : "")
		    .append(temporal)
		    .append("/");

		temporal = now.getMonthValue();
		text.append((temporal < 10) ? "0" : "")
		    .append(temporal)
		    .append("/")
		    .append(now.getYear())
		    .append(" ");

		temporal = now.getHour();
		text.append((temporal < 10) ? "0" : "")
		    .append(temporal)
		    .append(":");

		temporal = now.getMinute();
		text.append((temporal < 10) ? "0" : "")
		    .append(temporal)
		    .append(":");

		temporal = now.getSecond();
		text.append((temporal < 10) ? "0" : "")
		    .append(temporal);

		return setField("timestamp", text.toString());
	}


	public static class Config
	{
		public static Integer preallocSize = 64;

		public static String undefinedValue = "n/a";
	}
}
