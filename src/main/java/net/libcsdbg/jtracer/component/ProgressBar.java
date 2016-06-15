package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProgressBar extends JProgressBar implements AutoInjectable
{
	private static final long serialVersionUID = -4560634722419713338L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;


	protected String caption;


	public ProgressBar()
	{
		this(0, 100);
	}

	public ProgressBar(Integer min, Integer max)
	{
		this(min, max, "Initializing");
	}

	public ProgressBar(Integer min, Integer max, String caption)
	{
		super(min, max);
		selfInject();
		setCaption(caption);
		setString(caption + " - 0%");

		String widget = "progress-bar";
		setFont(componentSvc.getFont(widget));
		setForeground(componentSvc.getForegroundColor(widget));
		setBackground(componentSvc.getBackgroundColor(widget));

		setBorder(new EmptyBorder(0, 0, 0, 0));
		setBorderPainted(componentSvc.isEnabled("progress-bar-border"));
		setStringPainted(true);
	}

	public ProgressBar complete(Integer millis)
	{
		try {
			setValue(getMaximum());
			Thread.sleep(millis);
		}
		catch (InterruptedException ignored) {
		}

		return this;
	}

	public ProgressBar delay(Long millis)
	{
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException ignored) {
		}

		return this;
	}

	public ProgressBar setCaption(String caption)
	{
		if (caption == null) {
			return this;
		}

		caption = caption.trim();
		if (caption.length() == 0) {
			return this;
		}

		this.caption = caption;
		setToolTipText(caption);

		return this;
	}

	@Override
	public void setValue(int value)
	{
		/* If the call is within an event dispatching thread */
		if (SwingUtilities.isEventDispatchThread()) {
			try {
				super.setValue(value);

				if (caption != null) {
					Double percent = 100 * getPercentComplete();
					setString(caption + " - " + percent.intValue() + "%");
				}
			}
			catch (Throwable err) {
				loggerSvc.catching(getClass(), err);
			}

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
}
