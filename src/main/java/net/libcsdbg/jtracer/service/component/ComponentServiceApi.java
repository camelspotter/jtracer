package net.libcsdbg.jtracer.service.component;

import net.libcsdbg.jtracer.service.component.value.GridPresets;

import java.awt.*;

public interface ComponentServiceApi extends ComponentServiceState
{
	ComponentService activate();

	Color getBackgroundColor(String widget);

	Dimension getDimension(String widget);

	Font getFont(String widget);

	Color getForegroundColor(String widget);

	GridPresets getGridPresets(String widget);

	Insets getInsets(String widget);

	ComponentService passivate();
}
