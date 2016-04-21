package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class Alert extends JDialog implements ActionListener,
                                              AutoInjectable
{
	private static final long serialVersionUID = -9196393091054977304L;


	@Service
	protected LoggerService loggerSvc;

	@Service
	protected ComponentService componentSvc;

	@Service
	protected UtilityService utilitySvc;


	protected Boolean reply = false;


	public Alert()
	{
		super();
		selfInject();
	}

	public Alert(JFrame owner, String message, AlertType type)
	{
		super(owner, true);
		selfInject();

		Button ok = new Button("Ok", this);
		ok.setMnemonic(KeyEvent.VK_O);

		Button cancel = new Button("Cancel", this);
		cancel.setMnemonic(KeyEvent.VK_C);

		GridBagLayout layout = new GridBagLayout();
		JPanel center = new JPanel(layout);
		JPanel south = new JPanel(layout);

		GridBagConstraints bagConstraints = new GridBagConstraints();
		bagConstraints.gridy = 0;
		bagConstraints.gridx = 0;
		bagConstraints.insets = Config.buttonMargin;

		ImageIcon icon = null;
		switch (type) {
		case prompt:
			setTitle("Prompt");
			icon = utilitySvc.loadIcon("prompt32.png");

			layout.setConstraints(ok, bagConstraints);
			south.add(ok);

			bagConstraints.gridx++;
			layout.setConstraints(cancel, bagConstraints);
			south.add(cancel);
			break;

		case error:
			setTitle("Error");
			icon = utilitySvc.loadIcon("error32.png");
			layout.setConstraints(ok, bagConstraints);
			south.add(ok);
			break;

		case information:
			setTitle("Information");
			icon = utilitySvc.loadIcon("info32.png");
			layout.setConstraints(ok, bagConstraints);
			south.add(ok);
		}

		String lines[] = message.split("\\n");

		JLabel l = new JLabel(icon);
		bagConstraints.gridx = 0;
		bagConstraints.insets = Config.iconMargin;
		bagConstraints.anchor = GridBagConstraints.NORTH;
		bagConstraints.gridheight = lines.length;
		layout.setConstraints(l, bagConstraints);
		center.add(l);

		bagConstraints.gridx = 1;
		bagConstraints.gridheight = 1;
		bagConstraints.insets = Config.iconMargin;
		bagConstraints.anchor = GridBagConstraints.WEST;

		Font font = componentSvc.getFont("alert");
		Color foreground = componentSvc.getForegroundColor("alert");
		for (int i = 0; i < lines.length; i++) {
			l = new JLabel(lines[i]);
			l.setFont(font);
			l.setForeground(foreground);

			bagConstraints.gridy = i;
			layout.setConstraints(l, bagConstraints);
			center.add(l);
			bagConstraints.insets.top = 4;
		}

		add(center, BorderLayout.CENTER);
		add(south, BorderLayout.SOUTH);

		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(owner);

		getToolkit().beep();
		setVisible(true);
	}

	public static void error(JFrame owner, String message, Boolean isFatal)
	{
		new Alert(owner, message, AlertType.error);
		if (isFatal) {
			throw new RuntimeException(message);
		}
	}

	public static void information(JFrame owner, String message)
	{
		new Alert(owner, message, AlertType.information);
	}

	public static Boolean prompt(JFrame owner, String message)
	{
		return new Alert(owner, message, AlertType.prompt).getReply();
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		if (event.getActionCommand().equals("Ok")) {
			reply = true;
		}

		dispose();
	}

	public Boolean getReply()
	{
		return reply;
	}


	public static enum AlertType
	{
		error(0x01),

		information(0x02),

		prompt(0x04);


		protected Integer ordinal;

		private AlertType(Integer ordinal)
		{
			this.ordinal = ordinal;
		}
	}

	public static class Config
	{
		public static Insets buttonMargin = new Insets(12, 4, 4, 4);

		public static Insets iconMargin = new Insets(12, 12, 0, 12);
	}
}
