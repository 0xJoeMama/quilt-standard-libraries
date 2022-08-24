package org.quiltmc.qsl.registry.api.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

public final class QuiltRegistryBuilder<T> {
	private final List<RegistryBuilderModule<T>> modules;
	@Nullable
	private Function<T, Holder.Reference<T>> holderProvider;
	private Lifecycle lifecycle;

	private QuiltRegistryBuilder() {
		this.modules = new ArrayList<>();
		this.holderProvider = null;
		this.lifecycle = Lifecycle.experimental();
	}

	public static <T> QuiltRegistryBuilder<T> create() {
		return new QuiltRegistryBuilder<>();
	}

	public QuiltRegistryBuilder<T> addModule(RegistryBuilderModule<T> module) {
		this.modules.add(module);
		return this;
	}

	public QuiltRegistryBuilder<T> holderProvider(Function<T, Holder.Reference<T>> holderProvider) {
		this.holderProvider = holderProvider;
		return this;
	}

	public QuiltRegistryBuilder<T> lifecycle(Lifecycle lifecycle) {
		this.lifecycle = lifecycle;
		return this;
	}

	@SuppressWarnings("unchecked")
	public Registry<T> build(Identifier id) {
		RegistryKey<Registry<T>> regKey = RegistryKey.ofRegistry(id);
		SimpleRegistry<T> registry = new SimpleRegistry<>(regKey, this.lifecycle, this.holderProvider);

		for (var module : this.modules) {
			registry = module.applyModule(registry, regKey, this.lifecycle, this.holderProvider, this.modules);
		}

		Registry.register((Registry<Registry<T>>) Registry.REGISTRIES, regKey, registry);

		return registry;
	}

	public interface RegistryFactory<T> {
		SimpleRegistry<T> create(RegistryKey<Registry<T>> key, Lifecycle lifecycle,
				Function<T, Holder.Reference<T>> holderProvider, List<RegistryBuilderModule<T>> modules);
	}
}
