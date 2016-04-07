package net.libcsdbg.jtracer.service.component;

import net.libcsdbg.jtracer.service.component.value.GridPresets;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.registry.RegistryService;
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
import java.util.stream.Collectors;

@Mixins(ComponentService.Mixin.class)
@Activators(ComponentService.Activator.class)
public interface ComponentService extends ComponentServiceApi, ServiceComposite
{
	abstract class Mixin implements ComponentService
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

			metainfo().set("javax.swing");
			active().set(true);

			loggerSvc.info(getClass(), "Service '" + identity().get() + "' activated (" + metainfo().get() + ")");
			return this;
		}

		@Override
		public Color getBackgroundColor(String widget)
		{
			String param = registrySvc.get(widget + "-bgcolor");
			if (param == null) {
				return Color.lightGray;
			}

			return Color.decode(param);
		}

		@Override
		public Dimension getDimension(String widget)
		{
			Dimension retval = new Dimension(640, 480);

			String param = registrySvc.get(widget + "-size");
			if (param == null) {
				return retval;
			}

			String[] parts = param.split(",");
			List<Integer> axes =
				Arrays.asList(parts)
				      .stream()
				      .map(String::trim)
				      .map(Integer::parseInt)
				      .collect(Collectors.toList());

			retval.width = axes.get(0);
			retval.height = axes.get(1);
			return retval;
		}

		@Override
		public Font getFont(String widget)
		{
			String name = registrySvc.get(widget + "-font");
			String type = registrySvc.get(widget + "-font-type");
			String size = registrySvc.get(widget + "-font-size");

			if (name == null) {
				name = "Dialog";
			}

			int fontType = Font.PLAIN;
			if (type != null) {
				type = type.toLowerCase();

				if (type.equals("bold")) {
					fontType = Font.BOLD;
				}
				else if (type.equals("italic")) {
					fontType = Font.ITALIC;
				}
			}

			int fontSize = 12;
			if (size != null) {
				fontSize = Integer.parseInt(size);
			}

			return new Font(name, fontType, fontSize);
		}

		@Override
		public Color getForegroundColor(String widget)
		{
			String param = registrySvc.get(widget + "-fgcolor");
			if (param == null) {
				return Color.black;
			}

			return Color.decode(param);
		}

		@Override
		public GridPresets getGridPresets(String widget)
		{
			String param = registrySvc.get(widget + "-grid-presets");
			if (param == null) {
				return selfContainer.newValue(GridPresets.class);
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

		@Override
		public Insets getInsets(String widget)
		{
			Insets retval = new Insets(0, 0, 0, 0);

			String param = registrySvc.get(widget + "-insets");
			if (param == null) {
				return retval;
			}

			String[] parts = param.split(",");
			List<Integer> margins =
				Arrays.asList(parts)
				      .stream()
				      .map(String::trim)
				      .map(Integer::parseInt)
				      .collect(Collectors.toList());

			retval.top = margins.get(0);
			retval.left = margins.get(1);
			retval.bottom = margins.get(2);
			retval.right = margins.get(3);
			return retval;
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
