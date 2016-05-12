package net.libcsdbg.jtracer.core;

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
		registerLoggerService(module);
		registerRegistryService(module);
		registerComponentService(module);
		registerUtilityService(module);
		registerDictionaryService(module);

		module = layer.module("core");
		registerCoreObjects(module);

		module = layer.module("gui");
		registerComponents(module);

		return assembly;
	}

	protected Assembler registerComponentService(ModuleAssembly module)
	{
		module.values(GridPresets.class)
		      .visibleIn(Visibility.application);

		module.services(ComponentService.class)
		      .identifiedBy("Component Service")
		      .visibleIn(Visibility.application)
		      .instantiateOnStartup();

		return this;
	}

	protected Assembler registerComponents(ModuleAssembly module) throws AssemblyException
	{
		module.objects(AboutDialog.class,
		               Alert.class,
		               Button.class,
		               InputField.class,
		               InputPrompt.class,
		               LogPane.class,
		               MainFrame.class,
		               MenuBar.class,
		               Session.class,
		               SessionManager.class,
		               StatusBar.class,
		               ToolBar.class,
		               TracePane.class,
		               TraceStatusBar.class,
		               TraceToolBar.class)
		      .visibleIn(Visibility.application);

		return this;
	}

	protected Assembler registerCoreObjects(ModuleAssembly module) throws AssemblyException
	{
		module.objects(ApplicationCore.class,
		               GenericUncaughtExceptionHandler.class)
		      .visibleIn(Visibility.application);

		return this;
	}

	protected Assembler registerDictionaryService(ModuleAssembly module)
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

	protected Assembler registerLoggerService(ModuleAssembly module)
	{
		module.services(LoggerService.class)
		      .identifiedBy("Logger Service")
		      .visibleIn(Visibility.application)
		      .instantiateOnStartup();

		return this;
	}

	protected Assembler registerRegistryService(ModuleAssembly module)
	{
		module.services(RegistryService.class)
		      .identifiedBy("Registry Service")
		      .visibleIn(Visibility.application)
		      .instantiateOnStartup();

		return this;
	}

	protected Assembler registerUtilityService(ModuleAssembly module)
	{
		module.services(UtilityService.class)
		      .identifiedBy("Utility Service")
		      .visibleIn(Visibility.application)
		      .instantiateOnStartup();

		return this;
	}
}
