package net.libcsdbg.jtracer.service.graphics;

import net.libcsdbg.jtracer.service.graphics.value.GridPresets;
import org.qi4j.library.constraints.annotation.Matches;

import java.awt.*;

public interface ComponentServiceApi extends ComponentServiceState
{
	ComponentService activate();

	Color getBackgroundColor(@Matches("^[a-zA-Z0-9_-]+$") String widget);

	Dimension getDimension(@Matches("^[a-zA-Z0-9_-]+$") String widget);

	Font getFont(@Matches("^[a-zA-Z0-9_-]+$") String widget);

	String getFontName(@Matches("^[a-zA-Z0-9_-]+$") String widget);

	Integer getFontSize(@Matches("^[a-zA-Z0-9_-]+$") String widget);

	Integer getFontStyles(@Matches("^[a-zA-Z0-9_-]+$") String widget);

	Color getForegroundColor(@Matches("^[a-zA-Z0-9_-]+$") String widget);

	GridPresets getGridPresets(@Matches("^[a-zA-Z0-9_-]+$") String widget);

	Insets getInsets(@Matches("^[a-zA-Z0-9_-]+$") String widget);

	ComponentService passivate();
}
