package net.libcsdbg.jtracer.core;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;
import org.qi4j.tools.model.descriptor.ModuleDetailDescriptor;

public interface AutoInjectable
{
	default Application application()
	{
		return
			ApplicationCore.getCurrentApplicationCore()
			               .getApplication();
	}

	default Layer layer()
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

	default Module module()
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

	default AutoInjectable selfInject(Object... injected)
	{
		module().injectTo(this, injected);
		return this;
	}

	default AutoInjectable selfInject(Module module, Object... injected)
	{
		module.injectTo(this, injected);
		return this;
	}

	default AutoInjectable selfInject(String layer, String module, Object... injected)
	{
		Module m = application().findModule(layer, module);
		return selfInject(m, injected);
	}

	default AutoInjectable selfInject(Layer layer, String module, Object... injected)
	{
		Module m = application().findModule(layer.name(), module);
		return selfInject(m, injected);
	}
}
