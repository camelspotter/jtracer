package net.libcsdbg.jtracer.gui.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProgressBar extends JProgressBar implements AutoInjectable
{
	private static final long serialVersionUID = -4560634722419713338L;


	@Service
	protected ComponentService componentSvc;


	protected String caption;


	public ProgressBar()
	{
		this(0, 100);
	}

	public ProgressBar(Integer min, Integer max)
	{
		this(min, max, Config.defaultCaption);
	}

	public ProgressBar(Integer min, Integer max, String caption)
	{
		super(min, max);
		selfInject();

		setStringPainted(true);
		setCaption(caption);
		updateText();

		String widget = "progress-bar";
		setFont(componentSvc.getFont(widget));
		setForeground(componentSvc.getForegroundColor(widget));
		setBackground(componentSvc.getBackgroundColor(widget));

		setBorder(new EmptyBorder(0, 0, 0, 0));
		setBorderPainted(componentSvc.isEnabled(widget + "-border"));
	}

	public ProgressBar complete()
	{
		return complete(0);
	}

	public ProgressBar complete(Integer millis)
	{
		setValue(getMaximum());
		return stall(millis);
	}

	public ProgressBar setCaption(String caption)
	{
		this.caption = caption = (caption == null) ? "" : caption.trim();

		if (caption.length() > 0) {
			setToolTipText(caption);
		}

		return this;
	}

	@Override
	public void setValue(int value)
	{
		if (value <= getValue()) {
			return;
		}

		/* If the call is within an event dispatching thread */
		if (SwingUtilities.isEventDispatchThread()) {
			super.setValue(value);
			updateText();
			return;
		}

		/* Register a Runnable to be called by the event dispatching thread */
		try {
			SwingUtilities.invokeLater(() -> setValue(value));
		}
		catch (RuntimeException err) {
			throw err;
		}
		catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}

	public ProgressBar stall(Integer millis)
	{
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException ignored) {
		}

		return this;
	}

	protected ProgressBar updateText()
	{
		String text = (caption.length() > 0) ? caption + " - " : "";

		Double percentage = 100 * getPercentComplete();
		text += percentage.intValue() + "%";

		setString(text);
		return this;
	}


	public static class Config
	{
		public static String defaultCaption = "Initializing";
	}
}
