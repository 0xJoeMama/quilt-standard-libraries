package org.quiltmc.qsl.component.impl.container;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.component.api.ComponentFactory;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.component.NbtSerializable;
import org.quiltmc.qsl.component.api.component.Syncable;
import org.quiltmc.qsl.component.api.component.Tickable;

public final class ComponentInstance<T> {
	private static final byte INITIALIZED = 1;
	private static final byte NBT = 1 << 1;
	private static final byte TICK = 1 << 2;
	private static final byte SYNC = 1 << 3;


	@NotNull
	private final ComponentProvider provider;
	@NotNull
	private final ComponentFactory<T> factory;
	@Nullable
	private T component;
	private byte flags;

	public ComponentInstance(
			@NotNull ComponentProvider provider,
			@NotNull ComponentFactory<T> factory,
			boolean instaInit
	) {
		this.provider = provider;
		this.factory = factory;
		this.component = null;
		this.flags = 0;

		if (instaInit) {
			this.ensureInitialized();
		}
	}

	@NotNull
	public T get() {
		this.ensureInitialized();
		return this.component;
	}

	public boolean isInitialized() {
		return (this.flags & INITIALIZED) != 0;
	}

	public boolean needsSync() {
		return (this.flags & SYNC) != 0 && ((Syncable) this.component).needsSync();
	}

	public void writeNbt(ComponentType<?> type, NbtCompound providerRootNbt) {
		if ((this.flags & NBT) != 0) {
			NbtSerializable.writeTo(providerRootNbt, (NbtSerializable<?>) this.component, type.id());
		}
	}

	public void readNbt(ComponentType<?> type, NbtCompound providerRootNbt) {
		if ((this.flags & NBT) != 0) {
			NbtSerializable.readFrom(providerRootNbt, (NbtSerializable<?>) this.component, type.id());
		}
	}

	public void writeToBuf(PacketByteBuf buf) {
		((Syncable) this.component).writeToBuf(buf);
	}

	public void readFromBuf(PacketByteBuf buf) {
		this.ensureInitialized();

		((Syncable) this.component).readFromBuf(buf);
	}

	public void tick() {
		this.ensureInitialized();

		if ((this.flags & TICK) != 0) {
			((Tickable) this.component).tick();
		}
	}

	private void ensureInitialized() {
		if (!this.isInitialized()) {
			this.component = this.factory.create(this.provider);
			this.flags |= INITIALIZED;
			this.flags |= this.component instanceof NbtSerializable ? NBT : 0;
			this.flags |= this.component instanceof Tickable ? TICK : 0;
			this.flags |= this.component instanceof Syncable ? SYNC : 0;
		}
	}
}
