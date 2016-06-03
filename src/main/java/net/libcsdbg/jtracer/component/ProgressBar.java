package net.libcsdbg.jtracer.component;

import net.libcsdbg.jtracer.core.AutoInjectable;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.log.LoggerService;
import org.qi4j.api.injection.scope.Service;

import javax.swing.*;

public class ProgressBar extends JProgressBar implements AutoInjectable
{
	private static final long serialVersionUID = -4560634722419713338L;


	@Service
	protected ComponentService componentSvc;

	@Service
	protected LoggerService loggerSvc;


	protected String prefix;


	public ProgressBar()
	{
		this(0, 100);
	}

	public ProgressBar(Integer min, Integer max)
	{
		this(min, max, "");
	}

	public ProgressBar(Integer min, Integer max, String prefix)
	{
		super(min, max);
		selfInject();

		if (prefix != null && (prefix = prefix.trim()).length() > 0) {
			this.prefix = prefix;
			setToolTipText(prefix);
			setString(prefix + " - 0%");
		}

		String widget = "progress-bar";
		setFont(componentSvc.getFont(widget));
		setForeground(componentSvc.getForegroundColor(widget));
		setBackground(componentSvc.getBackgroundColor(widget));

		setBorderPainted(true);
		setStringPainted(true);
	}

	@Override
	public void setValue(int value)
	{
		/* If the call is within an event dispatching thread */
		if (SwingUtilities.isEventDispatchThread()) {
			try {
				super.setValue(value);

				if (prefix != null) {
					Double percent = 100 * getPercentComplete();
					setString(prefix + " - " + percent.intValue() + "%");
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
