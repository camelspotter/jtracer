void consolePrototype()
{
	JTextArea console=new JTextArea("jTracer> ");
	console.setForeground(Color.decode("0x76a6c7"));
	nowrap=new JPanel(new BorderLayout());
	nowrap.add(console);
	viewport=new JScrollPane(nowrap);
	viewport.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

	bagConstraints.weighty=0.25;
	bagConstraints.gridy++;

	layout.setConstraints(viewport,bagConstraints);
	inset.add(viewport);
}
