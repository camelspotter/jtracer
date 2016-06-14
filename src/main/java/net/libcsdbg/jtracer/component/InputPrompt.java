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
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;

	@Service
	protected UtilityService utilitySvc;


	protected Boolean cancelled;

	protected InputField inputField;


	private InputPrompt()
	{
	}

	public InputPrompt(JFrame owner, String message)
	{
		super(owner, "Prompt", true);
		selfInject();
		cancelled = true;

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints bagConstraints = new GridBagConstraints();

		Button ok = new Button("Ok", this);
		ok.setMnemonic(KeyEvent.VK_O);
		bagConstraints.gridy = 0;
		bagConstraints.gridx = 0;
		bagConstraints.insets = Config.buttonMargin;
		layout.setConstraints(ok, bagConstraints);

		Button cancel = new Button("Cancel", this);
		cancel.setMnemonic(KeyEvent.VK_C);
		bagConstraints.gridx++;
		layout.setConstraints(cancel, bagConstraints);

		JPanel south = new JPanel(layout);
		south.add(ok);
		south.add(cancel);
		add(south, BorderLayout.SOUTH);

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

		bagConstraints.gridx++;
		bagConstraints.insets = Config.textMargin;
		bagConstraints.anchor = GridBagConstraints.WEST;
		bagConstraints.gridheight = 1;

		Font font = componentSvc.getFont("alert");
		Color foreground = componentSvc.getForegroundColor("alert");
		for (int i = 0, count = lines.length; i < count; i++) {
			l = new JLabel(lines[i]);
			l.setFont(font);
			l.setForeground(foreground);

			bagConstraints.gridy = i;
			layout.setConstraints(l, bagConstraints);
			center.add(l);

			if (i == 0) {
				bagConstraints.insets.top =
					componentSvc.getLineSpacing("alert")
					            .intValue();
			}
		}

		inputField = new InputField("Ok", this);
		bagConstraints.gridy++;
		bagConstraints.ipadx = Config.inputFieldPadding;
		bagConstraints.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(inputField, bagConstraints);
		center.add(inputField);

		Checkbox check = new Checkbox("Case sensitive", this);
		check.setMnemonic(KeyEvent.VK_C);
		bagConstraints.gridy++;
		bagConstraints.ipadx = 0;
		bagConstraints.fill = GridBagConstraints.NONE;
		layout.setConstraints(check, bagConstraints);
		center.add(check);

		check = new Checkbox("Whole words only", this);
		check.setMnemonic(KeyEvent.VK_W);
		bagConstraints.gridy++;
		layout.setConstraints(check, bagConstraints);
		center.add(check);

		check = new Checkbox("Regular expression", this);
		check.setMnemonic(KeyEvent.VK_R);
		bagConstraints.gridy++;
		layout.setConstraints(check, bagConstraints);
		center.add(check);

		add(center, BorderLayout.CENTER);

		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		if (event.getSource() instanceof Checkbox) {
			return;
		}

		if (!event.getActionCommand().equals("Cancel")) {
			cancelled = false;
		}

		dispose();
	}

	public String getInput()
	{
		return (cancelled) ? null : inputField.getText();
	}

	@Override
	public void setVisible(boolean visible)
	{
		cancelled = true;
		super.setVisible(visible);
	}


	public static class Config
	{
		public static Integer inputFieldPadding = 200;


		public static Insets buttonMargin = new Insets(12, 4, 4, 4);

		public static Insets iconMargin = new Insets(12, 12, 0, 12);

		public static Insets textMargin = new Insets(12, 0, 0, 12);
	}
}
