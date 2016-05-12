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

public class InputPrompt extends JDialog implements ActionListener,
                                                    AutoInjectable
{
	private static final long serialVersionUID = 8285484182165866415L;


	@Service
	protected LoggerService loggerSvc;

	@Service
	protected ComponentService componentSvc;

	@Service
	protected UtilityService utilitySvc;


	protected TextInput input;

	protected String replyLatch;


	private InputPrompt()
	{
	}

	public InputPrompt(JFrame owner, String message)
	{
		super(owner, "Input prompt", true);
		selfInject();

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints bagConstraints = new GridBagConstraints();
		JPanel south = new JPanel(layout);

		Button ok = new Button("Ok", this);
		ok.setMnemonic(KeyEvent.VK_O);
		bagConstraints.gridy = 0;
		bagConstraints.gridx = 0;
		bagConstraints.insets = Config.buttonMargin;
		layout.setConstraints(ok, bagConstraints);
		south.add(ok);

		Button cancel = new Button("Cancel", this);
		cancel.setMnemonic(KeyEvent.VK_C);
		bagConstraints.gridx++;
		layout.setConstraints(cancel, bagConstraints);
		south.add(cancel);

		JPanel center = new JPanel(layout);
		ImageIcon icon = utilitySvc.loadIcon("prompt32.png");
		String lines[] = message.split("\\n");

		JLabel l = new JLabel(icon);
		bagConstraints.gridx = 0;
		bagConstraints.insets = Config.iconMargin;
		bagConstraints.anchor = GridBagConstraints.NORTH;
		bagConstraints.gridheight = lines.length + 1;
		layout.setConstraints(l, bagConstraints);
		center.add(l);

		bagConstraints.gridx = 1;
		bagConstraints.gridheight = 1;
		bagConstraints.insets = Config.textMargin;
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

		input = new TextInput((ActionListener) owner);
		input.setFont(font);
		bagConstraints.gridy++;
		bagConstraints.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(input, bagConstraints);
		center.add(input);

		add(center, BorderLayout.CENTER);
		add(south, BorderLayout.SOUTH);

		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(owner);

		getToolkit().beep();
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		if (event.getActionCommand().equals("Ok")) {
			replyLatch = input.getText();
		}

		dispose();
	}

	public String getReply()
	{
		return replyLatch;
	}


	public static class Config
	{
		public static Insets buttonMargin = new Insets(12, 4, 4, 4);

		public static Insets iconMargin = new Insets(12, 12, 0, 12);

		public static Insets textMargin = new Insets(12, 0, 0, 12);
	}
}
