package net.libcsdbg.jtracer.gui.container;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.gui.component.*;
import net.libcsdbg.jtracer.gui.component.Button;
import net.libcsdbg.jtracer.gui.component.Checkbox;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class SearchPrompt extends JDialog implements ActionListener,
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

	protected Checkbox[] options;


	private SearchPrompt()
	{
	}

	public SearchPrompt(JFrame owner, String message)
	{
		super(owner, "Search", true);
		selfInject();
		cancelled = true;

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints bagConstraints = new GridBagConstraints();

		Button ok = new Button("Ok", this);
		ok.setMnemonic(KeyEvent.VK_O);
		bagConstraints.gridx = 0;
		bagConstraints.gridy = 0;
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
		bagConstraints.gridheight = lines.length + 4;
		layout.setConstraints(l, bagConstraints);
		center.add(l);

		bagConstraints.gridx++;
		bagConstraints.insets = Config.textMargin;
		bagConstraints.anchor = GridBagConstraints.WEST;
		bagConstraints.gridheight = 1;

		Font font = componentSvc.getFont("alert");
		Color foreground = componentSvc.getForegroundColor("alert");
		for (int i = 0; i < lines.length; i++) {
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

		options = new Checkbox[3];
		Checkbox check = new Checkbox("Case sensitive", this);
		check.setMnemonic(KeyEvent.VK_S);
		check.setSelected(true);
		bagConstraints.gridy++;
		bagConstraints.ipadx = 0;
		bagConstraints.fill = GridBagConstraints.NONE;
		layout.setConstraints(check, bagConstraints);
		center.add(check);
		options[0] = check;

		check = new Checkbox("Whole words only", this);
		check.setMnemonic(KeyEvent.VK_W);
		bagConstraints.gridy++;
		layout.setConstraints(check, bagConstraints);
		center.add(check);
		options[1] = check;

		check = new Checkbox("Regular expression", this);
		check.setMnemonic(KeyEvent.VK_R);
		check.setSelected(true);
		bagConstraints.gridy++;
		layout.setConstraints(check, bagConstraints);
		center.add(check);
		options[2] = check;

		add(center, BorderLayout.CENTER);

		setResizable(false);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		if (event.getSource() instanceof Checkbox) {
			return;
		}

		String cmd = event.getActionCommand();
		if (cmd.equals("Ok")) {
			if (inputField.getText()
			              .trim()
			              .length() == 0) {
				return;
			}

			cancelled = false;
			Alert.information((JFrame) getOwner(), "Not implemented yet");
		}

		setVisible(false);
	}

	public String getInput()
	{
		return (cancelled) ? null : inputField.getText();
	}

	@Override
	public void setVisible(boolean visible)
	{
		if (visible) {
			cancelled = true;
		}

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
