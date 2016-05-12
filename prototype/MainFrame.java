public class MainFrame
{
	private MainFrame prototypeConsole()
	{
		Console c = new Console();
		JScrollPane viewport = new JScrollPane(c);
		viewport.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		JDialog dialog = new JDialog(this, "Console", true);
		dialog.add(viewport);
		dialog.setPreferredSize(new Dimension(640, 240));
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);

		return this;
	}
}
