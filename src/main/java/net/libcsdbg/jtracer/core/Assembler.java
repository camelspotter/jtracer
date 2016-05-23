package net.libcsdbg.jtracer.core;

import net.libcsdbg.jtracer.annotation.Factory;
import net.libcsdbg.jtracer.component.*;
import net.libcsdbg.jtracer.service.config.RegistryService;
import net.libcsdbg.jtracer.service.graphics.ComponentService;
import net.libcsdbg.jtracer.service.graphics.value.GridPresets;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.text.DictionaryService;
import net.libcsdbg.jtracer.service.text.parse.Token;
import net.libcsdbg.jtracer.service.text.parse.Tokenizer;
import net.libcsdbg.jtracer.service.util.UtilityService;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.*;

public class Assembler implements ApplicationAssembler
{
	protected ApplicationAssembly assembly;

	protected ApplicationProperties properties;


	public Assembler(ApplicationProperties properties)
	{
		this.properties = properties;
	}

	@Factory(Factory.Type.POJO)
	@Override
	public ApplicationAssembly assemble(ApplicationAssemblyFactory factory) throws AssemblyException
	{
		assembly =
			factory.newApplicationAssembly()
			       .setName(properties.getApplicationFullName())
			       .setMode(properties.getApplicationMode())
			       .setVersion(properties.getApplicationVersion());

		LayerAssembly layer = assembly.layer("application");
		ModuleAssembly module = layer.module("service");

		/* In dependency order */
		assembleLoggerService(module);
		assembleRegistryService(module);
		assembleComponentService(module);
		assembleUtilityService(module);
		assembleDictionaryService(module);

		module = layer.module("core");
		assembleCoreObjects(module);

		layer =
			assembly.layer("gui")
			        .uses(layer);

		module = layer.module("component");
		assembleComponents(module);

		module = layer.module("container");
		assembleContainers(module);

		return assembly;
	}

	protected Assembler assembleComponentService(ModuleAssembly module)
	{
		module.values(GridPresets.class)
		      .visibleIn(Visibility.application);

		module.services(ComponentService.class)
		      .identifiedBy("Component Service")
		      .visibleIn(Visibility.application)
		      .instantiateOnStartup();

		return this;
	}

	protected Assembler assembleComponents(ModuleAssembly module) throws AssemblyException
	{
		module.objects(Button.class,
		               Desktop.class,
		               InputField.class,
		               LogPane.class,
		               TracePane.class)
		      .visibleIn(Visibility.application);

		return this;
	}

	protected Assembler assembleContainers(ModuleAssembly module) throws AssemblyException
	{
		module.objects(AboutDialog.class,
		               Alert.class,
		               InputPrompt.class,
		               MainFrame.class,
		               MenuBar.class,
		               Session.class,
		               StatusBar.class,
		               ToolBar.class,
		               TraceStatusBar.class,
		               TraceToolBar.class)
		      .visibleIn(Visibility.application);

		return this;
	}

	protected Assembler assembleCoreObjects(ModuleAssembly module) throws AssemblyException
	{
		module.objects(ApplicationCore.class,
		               GenericUncaughtExceptionHandler.class)
		      .visibleIn(Visibility.application);

		return this;
	}

	protected Assembler assembleDictionaryService(ModuleAssembly module)
	{
		module.values(Token.class)
		      .visibleIn(Visibility.application);

		module.transients(Tokenizer.class)
		      .visibleIn(Visibility.application);

		module.services(DictionaryService.class)
		      .identifiedBy("Dictionary Service")
		      .visibleIn(Visibility.application)
		      .instantiateOnStartup();

		return this;
	}

	protected Assembler assembleLoggerService(ModuleAssembly module)
	{
		module.services(LoggerService.class)
		      .identifiedBy("Logger Service")
		      .visibleIn(Visibility.application)
		      .instantiateOnStartup();

		return this;
	}

	protected Assembler assembleRegistryService(ModuleAssembly module)
	{
		module.services(RegistryService.class)
		      .identifiedBy("Registry Service")
		      .visibleIn(Visibility.application)
		      .instantiateOnStartup();

		return this;
	}

	protected Assembler assembleUtilityService(ModuleAssembly module)
	{
		module.services(UtilityService.class)
		      .identifiedBy("Utility Service")
		      .visibleIn(Visibility.application)
		      .instantiateOnStartup();

		return this;
	}
}
