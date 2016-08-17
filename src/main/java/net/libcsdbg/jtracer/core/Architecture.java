package net.libcsdbg.jtracer.core;

import net.libcsdbg.jtracer.annotation.Factory;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.envisage.Envisage;
import org.qi4j.tools.model.descriptor.*;

import java.util.*;

public class Architecture
{
	protected final ApplicationDetailDescriptor rootModel;

	protected final Map<LayerDetailDescriptor, List<ModuleDetailDescriptor>> structure;


	public Architecture(ApplicationDescriptor descriptor)
	{
		rootModel = ApplicationDetailDescriptorBuilder.createApplicationDetailDescriptor(descriptor);
		structure = new HashMap<>();
		layers().stream()
		        .forEach(l -> structure.put(l, modules(l)));
	}

	protected static <T> List<T> asList(Iterable<T> source)
	{
		List<T> retval = new LinkedList<>();
		source.forEach(retval::add);
		return retval;
	}

	public ModuleDetailDescriptor findModule(Object obj)
	{
		return findModule(obj.getClass());
	}

	public ModuleDetailDescriptor findModule(Class<?> type)
	{
		if (TransientComposite.class.isAssignableFrom(type)) {
			return findModuleOfTransientComposite(type);
		}

		else if (ServiceComposite.class.isAssignableFrom(type)) {
			return findModuleOfServiceComposite(type);
		}

		else if (ValueComposite.class.isAssignableFrom(type)) {
			return findModuleOfValueComposite(type);
		}

		else {
			return findModuleOfNonComposite(type);
		}
	}

	public ModuleDetailDescriptor findModuleOfNonComposite(Class<?> type)
	{
		ModuleDetailDescriptor module =
			structure.values()
			         .stream()
			         .flatMap(Collection::stream)
			         .filter(m -> objects(m).stream()
			                                .anyMatch(o -> isContained(o, type)))
			         .findFirst()
			         .orElse(null);

		if (module != null) {
			return module;
		}

		throw new RuntimeException("No module found for non composite type " + type.getName());
	}

	public ModuleDetailDescriptor findModuleOfServiceComposite(Class<?> type)
	{
		ModuleDetailDescriptor module =
			structure.values()
			         .stream()
			         .flatMap(Collection::stream)
			         .filter(m -> services(m).stream()
			                                 .anyMatch(o -> isContained(o, type)))
			         .findFirst()
			         .orElse(null);

		if (module != null) {
			return module;
		}

		throw new RuntimeException("No module found for composite service type " + type.getName());
	}

	public ModuleDetailDescriptor findModuleOfTransientComposite(Class<?> type)
	{
		ModuleDetailDescriptor module =
			structure.values()
			         .stream()
			         .flatMap(Collection::stream)
			         .filter(m -> transients(m).stream()
			                                   .anyMatch(o -> isContained(o, type)))
			         .findFirst()
			         .orElse(null);

		if (module != null) {
			return module;
		}

		throw new RuntimeException("No module found for composite transient type " + type.getName());
	}

	public ModuleDetailDescriptor findModuleOfValueComposite(Class<?> type)
	{
		ModuleDetailDescriptor module =
			structure.values()
			         .stream()
			         .flatMap(Collection::stream)
			         .filter(m -> values(m).stream()
			                               .anyMatch(o -> isContained(o, type)))
			         .findFirst()
			         .orElse(null);

		if (module != null) {
			return module;
		}

		throw new RuntimeException("No module found for composite value object type " + type.getName());
	}

	public Boolean isContained(TransientDetailDescriptor container, Object obj)
	{
		return isContained(container, obj.getClass());
	}

	public Boolean isContained(TransientDetailDescriptor container, Class<?> type)
	{
		return
			asList(container.descriptor().types())
				.stream()
				.anyMatch(t -> t.equals(type));
	}

	public Boolean isContained(ObjectDetailDescriptor container, Object obj)
	{
		return isContained(container, obj.getClass());
	}

	public Boolean isContained(ObjectDetailDescriptor container, Class<?> type)
	{
		return
			asList(container.descriptor().types())
				.stream()
				.anyMatch(t -> t.equals(type));
	}

	public Boolean isContained(ServiceDetailDescriptor container, Object obj)
	{
		return isContained(container, obj.getClass());
	}

	public Boolean isContained(ServiceDetailDescriptor container, Class<?> type)
	{
		return
			asList(container.descriptor().types())
				.stream()
				.anyMatch(t -> t.equals(type));
	}

	public Boolean isContained(ValueDetailDescriptor container, Object obj)
	{
		return isContained(container, obj.getClass());
	}

	public Boolean isContained(ValueDetailDescriptor container, Class<?> type)
	{
		return
			asList(container.descriptor().types())
				.stream()
				.anyMatch(t -> t.equals(type));
	}

	public Boolean isSingleton()
	{
		List<LayerDetailDescriptor> layers = layers();
		if (layers.isEmpty()) {
			throw new IllegalStateException("Application '" + rootModel.descriptor().name() + "' has no layers");
		}

		if (layers.size() > 1) {
			return false;
		}

		LayerDetailDescriptor singleton = layers.get(0);
		List<ModuleDetailDescriptor> modules = modules(singleton);
		if (modules.isEmpty()) {
			throw new IllegalStateException("Singleton layer '" + singleton.descriptor().name() + "' has no modules");
		}

		return modules.size() == 1;
	}

	public LayerDetailDescriptor layer(String name)
	{
		LayerDetailDescriptor layer =
			layers().stream()
			        .filter(l -> l.descriptor()
			                      .name()
			                      .equals(name))
			        .findFirst()
			        .orElse(null);

		if (layer != null) {
			return layer;
		}

		throw new RuntimeException("Layer '" + name + "' not found in application '" + rootModel.descriptor().name() + "'");
	}

	public List<LayerDetailDescriptor> layers()
	{
		return asList(rootModel.layers());
	}

	public ModuleDetailDescriptor module(String layer, String name)
	{
		ModuleDetailDescriptor module =
			modules(layer).stream()
			              .filter(m -> m.descriptor()
			                            .name()
			                            .equals(name))
			              .findFirst()
			              .orElse(null);

		if (module != null) {
			return module;
		}

		throw new RuntimeException("Module '" + name + "' not found in layer '" + layer + "'");
	}

	public ModuleDetailDescriptor module(LayerDetailDescriptor layer, String name)
	{
		return module(layer.descriptor().name(), name);
	}

	public List<ModuleDetailDescriptor> modules(String layer)
	{
		return asList(layer(layer).modules());
	}

	public List<ModuleDetailDescriptor> modules(LayerDetailDescriptor layer)
	{
		return asList(layer.modules());
	}

	public List<ObjectDetailDescriptor> objects(String layer, String name)
	{
		return asList(module(layer, name).objects());
	}

	public List<ObjectDetailDescriptor> objects(ModuleDetailDescriptor module)
	{
		return asList(module.objects());
	}

	public List<ServiceDetailDescriptor> services(String layer, String name)
	{
		return asList(module(layer, name).services());
	}

	public List<ServiceDetailDescriptor> services(ModuleDetailDescriptor module)
	{
		return asList(module.services());
	}

	public final Map<LayerDetailDescriptor, List<ModuleDetailDescriptor>> structure()
	{
		return structure;
	}

	public List<TransientDetailDescriptor> transients(String layer, String name)
	{
		return asList(module(layer, name).transients());
	}

	public List<TransientDetailDescriptor> transients(ModuleDetailDescriptor module)
	{
		return asList(module.transients());
	}

	public List<ValueDetailDescriptor> values(String layer, String name)
	{
		return asList(module(layer, name).values());
	}

	public List<ValueDetailDescriptor> values(ModuleDetailDescriptor module)
	{
		return asList(module.values());
	}

	@Factory(Factory.Type.POJO)
	public Envisage visualize()
	{
		Envisage gui = new Envisage();
		gui.run(rootModel.descriptor());
		return gui;
	}
}
