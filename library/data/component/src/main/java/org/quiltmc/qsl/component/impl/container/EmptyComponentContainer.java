package org.quiltmc.qsl.component.impl.container;

import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;

import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.container.ComponentContainer;

public enum EmptyComponentContainer implements ComponentContainer {
	INSTANCE;

	@Override
	public <C> @Nullable C expose(ComponentType<C> type) {
		return null;
	}

	@Override
	public void writeNbt(NbtCompound providerRootNbt) { }

	@Override
	public void readNbt(NbtCompound providerRootNbt) { }

	@Override
	public void tick() { }

	@Override
	public void sync() { }

	@Override
	public void forEach(BiConsumer<ComponentType<?>, ? super Object> action) { }
}
