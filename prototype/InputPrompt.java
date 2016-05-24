public class InputPrompt extends JDialog implements ActionListener,
                                                    AutoInjectable
{
	public InputPrompt(JFrame owner, String message)
	{
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
	}
}
