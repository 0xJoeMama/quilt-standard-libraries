package org.quiltmc.qsl.component.impl.container;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.quiltmc.qsl.component.impl.util.StringConstants;

public class ComponentContainerImpl implements ComponentContainer {
	@NotNull
	private final ComponentProvider provider;
	private final Map<ComponentType<?>, ComponentInstance<?>> components;

	public ComponentContainerImpl(@NotNull ComponentProvider provider, Iterable<ComponentEntry<?>> entries) {
		this.provider = provider;

		var componentMap = new IdentityHashMap<ComponentType<?>, ComponentInstance<?>>();
		for (var entry : entries) {
			componentMap.computeIfAbsent(entry.type(), type -> new ComponentInstance<>(
					this.provider,
					entry.factory(),
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
	public void writeNbt(NbtCompound providerRootNbt) {
		var rootQslNbt = new NbtCompound();
		this.forEachInitialized((componentType, instance) -> instance.writeNbt(componentType, rootQslNbt));
		providerRootNbt.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
	}

	@Override
	public void readNbt(NbtCompound providerRootNbt) {
		var rootQslNbt = providerRootNbt.getCompound(StringConstants.COMPONENT_ROOT);
		for (String key : rootQslNbt.getKeys()) {
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
		SyncChannel<?, ?> syncChannel = this.provider.getSyncChannel();

		if (syncChannel != null) {
			var queue = new ArrayList<ComponentType<?>>(2);
			this.forEachInitialized((type, instance) -> {
				if (instance.needsSync()) {
					queue.add(type);
				}
			});
			syncChannel.sync(queue, this.provider);
		}
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
