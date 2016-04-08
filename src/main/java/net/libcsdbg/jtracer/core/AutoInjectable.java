package net.libcsdbg.jtracer.core;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;
import org.qi4j.tools.model.descriptor.ModuleDetailDescriptor;

public interface AutoInjectable
{
	default Application getApplication()
	{
		return ApplicationCore.getCurrentApplicationCore().getApplication();
	}

	default Layer getLayer()
	{
		ModuleDetailDescriptor module =
			ApplicationCore.getCurrentApplicationCore()
			               .getArchitecture()
			               .findModule(this);

		String layerName =
			module.layer()
			      .descriptor()
			      .name();

		return getApplication().findLayer(layerName);
	}

	default Module getModule()
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

		return getApplication().findModule(layerName, moduleName);
	}

	default AutoInjectable selfInject(Object... injected)
	{
		getModule().injectTo(this, injected);
		return this;
	}

	default AutoInjectable selfInject(Module module, Object... injected)
	{
		module.injectTo(this, injected);
		return this;
	}

	default AutoInjectable selfInject(String layer, String module, Object... injected)
	{
		Module m = getApplication().findModule(layer, module);
		return selfInject(m, injected);
	}

	default AutoInjectable selfInject(Layer layer, String module, Object... injected)
	{
		Module m = getApplication().findModule(layer.name(), module);
		return selfInject(m, injected);
	}
}
