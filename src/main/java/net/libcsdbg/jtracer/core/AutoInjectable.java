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
		String name =
			ApplicationCore.getCurrentApplicationCore()
			               .getArchitecture()
			               .findModule(this)
			               .layer()
			               .descriptor()
			               .name();

		return application().findLayer(name);
	}

	public default Module module()
	{
		ModuleDetailDescriptor container =
			ApplicationCore.getCurrentApplicationCore()
			               .getArchitecture()
			               .findModule(this);

		String module =
			container.descriptor()
			         .name();

		String layer =
			container.layer()
			         .descriptor()
			         .name();

		return application().findModule(layer, module);
	}

	public default Module module(String layer, String module)
	{
		return application().findModule(layer, module);
	}

	public default Module module(Layer layer, String module)
	{
		return application().findModule(layer.name(), module);
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
		return selfInject(module(layer, module), injected);
	}

	public default AutoInjectable selfInject(Layer layer, String module, Object... injected)
	{
		return selfInject(module(layer, module), injected);
	}
}
