package org.quiltmc.qsl.component.impl.container;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;

public class ComponentContainerImpl implements ComponentContainer {
	private final ComponentProvider provider;
	private final Map<ComponentType<?>, ComponentInstance<?>> components;

	public ComponentContainerImpl(ComponentProvider provider, Iterable<ComponentEntry<?>> entries) {
		this.provider = provider;

		var componentMap = new IdentityHashMap<ComponentType<?>, ComponentInstance<?>>();
		for (var entry : entries) {
			componentMap.computeIfAbsent(entry.type(), type -> new ComponentInstance<>(
					entry.factory(),
					this.provider,
					type.isInstant()
			));
		}

		this.components = componentMap;
	}

	@Override
	public <C> @Nullable C expose(ComponentType<C> type) {
		return this.components.containsKey(type) ? type.cast(this.components.get(type).get()) : null;
	}

	@Override
	public ComponentProvider getProvider() {
		return this.provider;
	}

	@Override
	public void writeNbt(NbtCompound providerRootNbt) {
		this.forEachInitialized((componentType, instance) -> instance.writeNbt(componentType, providerRootNbt));
	}

	@Override
	public void readNbt(NbtCompound providerRootNbt) {
		for (String key : providerRootNbt.getKeys()) {
			var id = Identifier.tryParse(key);
			if (id == null) continue;

			var type = Components.REGISTRY.get(id);
			if (type == null) continue;

			var instance = this.components.get(type);
			instance.readNbt(type, providerRootNbt);
		}
	}

	@Override
	public void tick() {
		for (var instance : this.components.values()) {
			instance.tick();
		}
	}

	@Override
	public void sync() {
		var queue = new ArrayDeque<ComponentType<?>>(this.components.size() / 3);

		for (var entry : this.components.entrySet()) {
			if (entry.getValue().needsSync()) {
				queue.add(entry.getKey());
			}
		}

		this.provider.getSyncChannel().syncFromQueue(queue, this.provider);
	}

	@Override
	public void forEach(BiConsumer<ComponentType<?>, ? super Object> action) {
		this.forEachInitialized((componentType, instance) -> action.accept(componentType, instance.get()));
	}

	private void forEachInitialized(BiConsumer<ComponentType<?>, ComponentInstance<?>> action) {
		for (var entry : this.components.entrySet()) {
			if (entry.getValue().isInitialized()) {
				action.accept(entry.getKey(), entry.getValue());
			}
		}
	}
}
