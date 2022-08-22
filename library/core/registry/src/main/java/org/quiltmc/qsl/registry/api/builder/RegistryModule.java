package org.quiltmc.qsl.registry.api.builder;

import java.util.function.Consumer;

import net.minecraft.util.registry.SimpleRegistry;

import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.registry.api.event.RegistryMonitor;
import org.quiltmc.qsl.registry.api.sync.RegistrySynchronization;

public final class RegistryModule<T> {
	private final Consumer<? super SimpleRegistry<T>> onCreate;

	private RegistryModule(Consumer<? super SimpleRegistry<T>> onCreate) {
		this.onCreate = onCreate;
	}

	public static <T> RegistryModule<T> onAdded(RegistryEvents.EntryAdded<T> callback) {
		return new RegistryModule<>(
			objects -> RegistryMonitor.create(objects).forAll(callback)
		);
	}

	public static <T> RegistryModule<T> synchronize() {
		return new RegistryModule<>(RegistrySynchronization::markForSync);
	}

	public void applyModule(SimpleRegistry<T> registry) {
		this.onCreate.accept(registry);
	}
}
