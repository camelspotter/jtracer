package net.libcsdbg.jtracer.service.graphics;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.annotation.MixinNote;
import net.libcsdbg.jtracer.annotation.constraint.WidgetDescriptor;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.graphics.value.GridPresets;
import net.libcsdbg.jtracer.service.log.LoggerService;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Mixins(ComponentService.Mixin.class)
@Activators(ComponentService.Activator.class)
public interface ComponentService extends ComponentServiceApi,
                                          ServiceComposite
{
	@MixinNote("The default service implementation uses AWT")
	public abstract class Mixin implements ComponentService
	{
		@Structure
		protected Module selfContainer;

		@Service
		protected LoggerService loggerSvc;

		@Service
		protected RegistryService registrySvc;


		@Override
		public ComponentService activate()
		{
			if (active().get()) {
				return this;
			}

			metainfo().set("java.awt");
			active().set(true);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' activated (" + metainfo().get() + ")");
			return this;
		}

		@Factory(Factory.Type.POJO)
		@Override
		public Color getBackgroundColor(String widget)
		{
			String key = widget + "-bgcolor";
			String param = registrySvc.get(key);
			if (param == null) {
				throw new RuntimeException("No configuration found for key '" + key + "'");
			}

			return Color.decode(param.trim());
		}

		@Factory(Factory.Type.POJO)
		@Override
		public Color getCaretColor(@WidgetDescriptor String widget)
		{
			String key = widget + "-caret-color";
			String param = registrySvc.get(key);
			if (param == null) {
				throw new RuntimeException("No configuration found for key '" + key + "'");
			}

			return Color.decode(param.trim());
		}

		@Factory(Factory.Type.POJO)
		@Override
		public Dimension getDimension(String widget)
		{
			String key = widget + "-size";
			String param = registrySvc.get(key);
			if (param == null) {
				throw new RuntimeException("No configuration found for key '" + key + "'");
			}

			String[] parts = param.split(",");
			List<Integer> axes =
				Arrays.asList(parts)
				      .stream()
				      .map(String::trim)
				      .map(Integer::parseInt)
				      .collect(Collectors.toList());

			return new Dimension(axes.get(0), axes.get(1));
		}

		@Factory(Factory.Type.POJO)
		@SuppressWarnings("MagicConstant")
		@Override
		public Font getFont(String widget)
		{
			return new Font(getFontName(widget), getFontStyles(widget), getFontSize(widget));
		}

		@Override
		public String getFontName(String widget)
		{
			String key = widget + "-font";
			String param = registrySvc.get(key);
			if (param == null) {
				throw new RuntimeException("No configuration found for key '" + key + "'");
			}

			return param.trim();
		}

		@Override
		public Integer getFontSize(String widget)
		{
			String key = widget + "-font-size";
			String param = registrySvc.get(key);
			if (param == null) {
				throw new RuntimeException("No configuration found for key '" + key + "'");
			}

			return Integer.parseInt(param.trim());
		}

		@Override
		public Integer getFontStyles(String widget)
		{
			String key = widget + "-font-type";
			String param = registrySvc.get(key);
			if (param == null) {
				throw new RuntimeException("No configuration found for key '" + key + "'");
			}

			int retval = 0;
			for (String style : param.split(",")) {
				style = style.trim().toLowerCase();

				switch (style) {
				case "plain":
					retval |= Font.PLAIN;
					break;

				case "bold":
					retval |= Font.BOLD;
					break;

				case "italic":
					retval |= Font.ITALIC;
					break;

				default:
					loggerSvc.warning(getClass(), "Unknown font style '" + style + "' for key '" + key + "'");
				}
			}

			return retval;
		}

		@Factory(Factory.Type.POJO)
		@Override
		public Color getForegroundColor(String widget)
		{
			String key = widget + "-fgcolor";
			String param = registrySvc.get(key);
			if (param == null) {
				throw new RuntimeException("No configuration found for key '" + key + "'");
			}

			return Color.decode(param.trim());
		}

		@Factory(Factory.Type.COMPOSITE_VALUE)
		@Override
		public GridPresets getGridPresets(String widget)
		{
			String key = widget + "-grid-presets";
			String param = registrySvc.get(key);
			if (param == null) {
				throw new RuntimeException("No configuration found for key '" + key + "'");
			}

			String[] parts = param.split(",");
			List<Integer> offsets =
				Arrays.asList(parts)
				      .stream()
				      .map(String::trim)
				      .map(Integer::parseInt)
				      .collect(Collectors.toList());

			ValueBuilder<GridPresets> builder = selfContainer.newValueBuilder(GridPresets.class);

			builder.prototype()
			       .rowSize()
			       .set(offsets.get(0));

			builder.prototype()
			       .step()
			       .set(offsets.get(1));

			builder.prototype()
			       .baseX()
			       .set(offsets.get(2));

			builder.prototype()
			       .baseY()
			       .set(offsets.get(3));

			return builder.newInstance();
		}

		@Factory(Factory.Type.POJO)
		@Override
		public Insets getInsets(String widget)
		{
			String key = widget + "-insets";
			String param = registrySvc.get(key);
			if (param == null) {
				throw new RuntimeException("No configuration found for key '" + key + "'");
			}

			String[] parts = param.split(",");
			List<Integer> margins =
				Arrays.asList(parts)
				      .stream()
				      .map(String::trim)
				      .map(Integer::parseInt)
				      .collect(Collectors.toList());

			return
				new Insets(margins.get(0),
				           margins.get(1),
				           margins.get(2),
				           margins.get(3));
		}

		@Factory(Factory.Type.POJO)
		@Override
		public Integer getLineSpacing(@WidgetDescriptor String widget)
		{
			String key = widget + "-line-spacing";
			String param = registrySvc.get(key);
			if (param == null) {
				throw new RuntimeException("No configuration found for key '" + key + "'");
			}

			return Integer.parseInt(param.trim());
		}

		@Factory(Factory.Type.POJO)
		@Override
		public Locale getLocale(@WidgetDescriptor String widget)
		{
			String key = widget + "-locale";
			String param = registrySvc.get(key);
			if (param == null) {
				throw new RuntimeException("No configuration found for key '" + key + "'");
			}

			param = param.trim();
			for (Locale l : Locale.getAvailableLocales()) {
				if (param.equals(l.toLanguageTag())) {
					return l;
				}
			}

			throw new RuntimeException("No locale found with language tag '" + param + "'");
		}

		@Override
		public Boolean isEnabled(@WidgetDescriptor String graphicsOption)
		{
			String param = registrySvc.get(graphicsOption);
			if (param == null) {
				throw new RuntimeException("No configuration found for key '" + graphicsOption + "'");
			}

			param = param.trim().toLowerCase();
			return
				param.equals("true") ||
				param.equals("yes") ||
				param.equals("on") ||
				param.equals("1");
		}

		@Override
		public ComponentService passivate()
		{
			if (!active().get()) {
				return this;
			}

			active().set(false);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' passivated (" + metainfo().get() + ")");
			return this;
		}
	}


	class Activator extends ActivatorAdapter<ServiceReference<ComponentService>>
	{
		@Override
		public void afterActivation(ServiceReference<ComponentService> svc) throws Exception
		{
			svc.get()
			   .active()
			   .set(false);

			svc.get().activate();
		}

		@Override
		public void beforePassivation(ServiceReference<ComponentService> svc) throws Exception
		{
			svc.get().passivate();
		}
	}
}
