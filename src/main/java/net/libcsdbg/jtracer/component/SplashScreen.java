package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JDialog implements AutoInjectable
{
	private static final long serialVersionUID = -1203622404963547880L;


	@Service
	protected UtilityService utilitySvc;


	protected final ProgressBar progress;


	public SplashScreen()
	{
		super();
		selfInject();
		setUndecorated(true);

		try {
			JTextPane viewer = new JTextPane();

			viewer.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
			viewer.putClientProperty(JEditorPane.W3C_LENGTH_UNITS, true);
			viewer.setPage(utilitySvc.getResource(Config.page)
			                         .toURI()
			                         .toURL());

			viewer.setEditable(false);
			viewer.setFocusable(false);
			viewer.setMargin(Config.preferredMargin);
			viewer.setPreferredSize(Config.preferredSize);
			add(viewer, BorderLayout.CENTER);

			progress = new ProgressBar(0, 100, "Initializing");
			add(progress, BorderLayout.SOUTH);

			setResizable(false);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			pack();

			setAlwaysOnTop(true);
			setLocationRelativeTo(null);
			setVisible(true);
		}
		catch (RuntimeException err) {
			dispose();
			throw err;
		}
		catch (Throwable err) {
			dispose();
			throw new RuntimeException(err);
		}
	}

	public final ProgressBar getProgress()
	{
		return progress;
	}


	public static class Config
	{
		public static String page = "theme/common/splash.html";

		public static Insets preferredMargin = new Insets(0, 0, 0, 0);

		public static Dimension preferredSize = new Dimension(380, 160);
	}
}
