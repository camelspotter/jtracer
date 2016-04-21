package net.libcsdbg.jtracer.service.graphics;

import net.libcsdbg.jtracer.annotation.constraint.WidgetDescriptor;
import net.libcsdbg.jtracer.annotation.constraint.WidgetDescriptorConstraint;
import net.libcsdbg.jtracer.service.graphics.value.GridPresets;
import org.qi4j.api.constraint.Constraints;

import java.awt.*;

@Constraints(WidgetDescriptorConstraint.class)
public interface ComponentServiceApi extends ComponentServiceState
{
	ComponentService activate();

	Color getBackgroundColor(@WidgetDescriptor String widget);

	Dimension getDimension(@WidgetDescriptor String widget);

	Font getFont(@WidgetDescriptor String widget);

	String getFontName(@WidgetDescriptor String widget);

	Integer getFontSize(@WidgetDescriptor String widget);

	Integer getFontStyles(@WidgetDescriptor String widget);

	Color getForegroundColor(@WidgetDescriptor String widget);

	GridPresets getGridPresets(@WidgetDescriptor String widget);

	Insets getInsets(@WidgetDescriptor String widget);

	ComponentService passivate();
}
