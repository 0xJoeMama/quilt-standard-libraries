package org.quiltmc.qsl.registry.api.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

public final class QuiltRegistryBuilder<T> {
	private final List<RegistryModule<T>> modules;
	@Nullable
	private Function<T, Holder.Reference<T>> holderProvider;
	@Nullable
	private Identifier defaultInstance;
	private Lifecycle lifecycle;

	private QuiltRegistryBuilder() {
		this.modules = new ArrayList<>();
		this.holderProvider = null;
		this.defaultInstance = null;
		this.lifecycle = Lifecycle.experimental();
	}

	public static <T> QuiltRegistryBuilder<T> create() {
		return new QuiltRegistryBuilder<>();
	}

	public QuiltRegistryBuilder<T> addModule(RegistryModule<T> module) {
		this.modules.add(module);
		return this;
	}

	public QuiltRegistryBuilder<T> holderProvider(Function<T, Holder.Reference<T>> holderProvider) {
		this.holderProvider = holderProvider;
		return this;
	}

	public QuiltRegistryBuilder<T> defaulted(Identifier defaultInstance) {
		this.defaultInstance = defaultInstance;
		return this;
	}

	public QuiltRegistryBuilder<T> lifecycle(Lifecycle lifecycle) {
		this.lifecycle = lifecycle;
		return this;
	}

	@SuppressWarnings("unchecked")
	public Registry<T> build(Identifier id) {
		RegistryKey<Registry<T>> regKey = RegistryKey.ofRegistry(id);
		SimpleRegistry<T> registry;

		if (this.defaultInstance != null) {
			registry = new DefaultedRegistry<>(
					this.defaultInstance.toString(),
					regKey,
					this.lifecycle,
					this.holderProvider
			);
		} else {
			registry = new SimpleRegistry<>(regKey, this.lifecycle, this.holderProvider);
		}

		Registry.register((Registry<Registry<T>>) Registry.REGISTRIES, regKey, registry);

		this.modules.forEach(module -> module.applyModule(registry));
		return registry;
	}
}
