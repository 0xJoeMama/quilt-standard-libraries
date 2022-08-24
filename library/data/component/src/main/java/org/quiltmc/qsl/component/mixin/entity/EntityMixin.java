package org.quiltmc.qsl.component.mixin.entity;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;

import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.container.ComponentContainerImpl;

@Mixin(Entity.class)
public abstract class EntityMixin implements ComponentProvider {
	private final ComponentContainer qsl$container =
			new ComponentContainerImpl(this, ComponentsImpl.getInjections(this));

	@Override
	public ComponentContainer getComponentContainer() {
		return this.qsl$container;
	}

	@Override
	public @Nullable SyncChannel<?, ?> getSyncChannel() {
		return SyncChannel.ENTITY;
	}

	@Inject(method = "writeNbt", at = @At("RETURN"))
	private void writeComponentNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
		this.qsl$container.writeNbt(nbt);
	}

	@Inject(method = "readNbt", at = @At("RETURN"))
	private void readComponentNbt(NbtCompound nbt, CallbackInfo ci) {
		this.qsl$container.readNbt(nbt);
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void tickComponents(CallbackInfo ci) {
		this.qsl$container.tick();
		// TODO: :concern:
		this.qsl$container.sync();
	}
}
