package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.registry.RegistryService;
import net.libcsdbg.jtracer.service.utility.UtilityService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.File;
import java.net.URL;

public class AboutDialog extends JDialog implements HyperlinkListener,
                                                    AutoInjectable
{
	private static final long serialVersionUID = -3746398566140659455L;


	@Service
	protected LoggerService loggerSvc;

	@Service
	protected RegistryService registrySvc;

	@Service
	protected UtilityService utilitySvc;


	public AboutDialog()
	{
		super();
		selfInject();
	}

	public AboutDialog(JFrame owner)
	{
		super(owner, true);
		selfInject();
		setTitle("About " + registrySvc.get("name"));

		try {
			File page = utilitySvc.getResource("theme/common/about.html");
			JEditorPane viewer = new JEditorPane(page.toURI().toURL());

			viewer.putClientProperty(JEditorPane.W3C_LENGTH_UNITS, true);
			viewer.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
			viewer.setEditable(false);
			viewer.setFocusable(false);
			viewer.setMargin(new Insets(0, 0, 0, 0));
			viewer.setPreferredSize(new Dimension(360, 406));

			viewer.addHyperlinkListener(this);
			add(viewer, BorderLayout.CENTER);
		}
		catch (RuntimeException err) {
			dispose();
			throw err;
		}
		catch (Throwable err) {
			dispose();
			throw new RuntimeException(err);
		}

		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
	}

	public void hyperlinkUpdate(HyperlinkEvent event)
	{
		loggerSvc.trace(getClass(), event.toString());

		try {
			if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
				return;
			}

			URL url = event.getURL();
			String proto = url.getProtocol();

			switch (proto) {
			case "file":
				setVisible(false);
				break;

			case "mailto":
				utilitySvc.mailTo(url);
				break;

			case "http":
				utilitySvc.browse(url);
			}
		}
		catch (Throwable err) {
			loggerSvc.catching(getClass(), err);
		}
	}
}
