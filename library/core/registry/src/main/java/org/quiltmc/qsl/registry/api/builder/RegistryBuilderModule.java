package org.quiltmc.qsl.registry.api.builder;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.serialization.Lifecycle;

import net.minecraft.util.Holder;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.registry.api.event.RegistryMonitor;
import org.quiltmc.qsl.registry.api.sync.RegistrySynchronization;

public interface RegistryBuilderModule<T> {
	static <T> RegistryBuilderModule<T> onAdded(RegistryEvents.EntryAdded<T> callback) {
		return new ActionModule<>(
				objects -> RegistryMonitor.create(objects).forAll(callback)
		);
	}

	static <T> RegistryBuilderModule<T> synchronizing() {
		return new ActionModule<>(RegistrySynchronization::markForSync);
	}

	static <T> RegistryBuilderModule<T> customFactory(QuiltRegistryBuilder.RegistryFactory<T> factory) {
		return new RegistryBuilderModule.CustomFactoryModule<>(factory);
	}

	SimpleRegistry<T> applyModule(SimpleRegistry<T> oldRegistry, RegistryKey<Registry<T>> key, Lifecycle lifecycle,
			Function<T, Holder.Reference<T>> holderProvider, List<RegistryBuilderModule<T>> modules);

	class ActionModule<T> implements RegistryBuilderModule<T> {
		private final Consumer<SimpleRegistry<T>> action;

		private ActionModule(Consumer<SimpleRegistry<T>> action) {
			this.action = action;
		}

		@Override
		public SimpleRegistry<T> applyModule(SimpleRegistry<T> oldRegistry, RegistryKey<Registry<T>> key,
				Lifecycle lifecycle, Function<T, Holder.Reference<T>> holderProvider,
				List<RegistryBuilderModule<T>> registryBuilderModules) {
			this.action.accept(oldRegistry);
			return oldRegistry;
		}
	}

	class ModifyModule<T> implements RegistryBuilderModule<T> {
		private final Function<SimpleRegistry<T>, SimpleRegistry<T>> modifier;

		private ModifyModule(Function<SimpleRegistry<T>, SimpleRegistry<T>> modifier) {
			this.modifier = modifier;
		}

		@Override
		public SimpleRegistry<T> applyModule(SimpleRegistry<T> oldRegistry, RegistryKey<Registry<T>> key,
				Lifecycle lifecycle, Function<T, Holder.Reference<T>> holderProvider,
				List<RegistryBuilderModule<T>> registryBuilderModules) {
			return this.modifier.apply(oldRegistry);
		}
	}

	class CustomFactoryModule<T> implements RegistryBuilderModule<T> {
		private final QuiltRegistryBuilder.RegistryFactory<T> factory;

		private CustomFactoryModule(QuiltRegistryBuilder.RegistryFactory<T> factory) {
			this.factory = factory;
		}

		@Override
		public SimpleRegistry<T> applyModule(SimpleRegistry<T> regildRegistrystry, RegistryKey<Registry<T>> key,
				Lifecycle lifecycle, Function<T, Holder.Reference<T>> holderProvider,
				List<RegistryBuilderModule<T>> modules) {
			SimpleRegistry<T> newRegistry = this.factory.create(key, lifecycle, holderProvider, modules);

			for (var module : modules) {
				if (!(module instanceof RegistryBuilderModule.CustomFactoryModule<?>))  {
					newRegistry = module.applyModule(newRegistry, key, lifecycle, holderProvider, modules);
				}
			}

			return newRegistry;
		}
	}
}
