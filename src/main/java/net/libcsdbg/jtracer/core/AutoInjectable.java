package net.libcsdbg.jtracer.core;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;
import org.qi4j.tools.model.descriptor.ModuleDetailDescriptor;

public interface AutoInjectable
{
	public default Application application()
	{
		return
			ApplicationCore.getCurrentApplicationCore()
			               .getApplication();
	}

	public default Layer layer()
	{
		ModuleDetailDescriptor module =
			ApplicationCore.getCurrentApplicationCore()
			               .getArchitecture()
			               .findModule(this);

		String layerName =
			module.layer()
			      .descriptor()
			      .name();

		return application().findLayer(layerName);
	}

	public default Module module()
	{
		ModuleDetailDescriptor module =
			ApplicationCore.getCurrentApplicationCore()
			               .getArchitecture()
			               .findModule(this);

		String moduleName = module.descriptor().name();

		String layerName =
			module.layer()
			      .descriptor()
			      .name();

		return application().findModule(layerName, moduleName);
	}

	public default AutoInjectable selfInject(Object... injected)
	{
		return selfInject(module(), injected);
	}

	public default AutoInjectable selfInject(Module module, Object... injected)
	{
		module.injectTo(this, injected);
		return this;
	}

	public default AutoInjectable selfInject(String layer, String module, Object... injected)
	{
		return selfInject(application().findModule(layer, module), injected);
	}

	public default AutoInjectable selfInject(Layer layer, String module, Object... injected)
	{
		return selfInject(application().findModule(layer.name(), module), injected);
	}
}
