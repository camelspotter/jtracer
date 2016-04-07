package net.libcsdbg.jtracer.core;

import net.libcsdbg.jtracer.component.*;
import net.libcsdbg.jtracer.service.component.ComponentService;
import net.libcsdbg.jtracer.service.component.value.GridPresets;
import net.libcsdbg.jtracer.service.log.LoggerService;
import net.libcsdbg.jtracer.service.parser.ParserService;
import net.libcsdbg.jtracer.service.registry.RegistryService;
import net.libcsdbg.jtracer.service.utility.UtilityService;
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
		assembly = factory.newApplicationAssembly();
		assembly.setMode(properties.getApplicationMode())
		        .setVersion(properties.getApplicationVersion())
		        .setName("jTracer");

		LayerAssembly layer = assembly.layer("application");
		ModuleAssembly module = layer.module("service");

		registerLoggerService(module);
		registerRegistryService(module);
		registerComponentService(module);
		registerUtilityService(module);
		registerParserService(module);

		module = layer.module("util");
		registerObjects(module);

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

	protected Assembler registerLoggerService(ModuleAssembly module)
	{
		module.services(LoggerService.class)
		      .identifiedBy("Logger Service")
		      .visibleIn(Visibility.application)
		      .instantiateOnStartup();

		return this;
	}

	protected Assembler registerObjects(ModuleAssembly module) throws AssemblyException
	{
		module.objects(AboutDialog.class,
		               Alert.class,
		               Button.class,
		               LogPane.class,
		               MainFrame.class,
		               MenuBar.class,
		               Session.class,
		               SessionManager.class,
		               StatusBar.class,
		               ToolBar.class,
		               TraceBar.class,
		               TracePane.class,

		               ApplicationCore.class,
		               GenericUncaughtExceptionHandler.class)
		      .visibleIn(Visibility.application);

		return this;
	}

	protected Assembler registerParserService(ModuleAssembly module)
	{
		module.services(ParserService.class)
		      .identifiedBy("Parser Service")
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
