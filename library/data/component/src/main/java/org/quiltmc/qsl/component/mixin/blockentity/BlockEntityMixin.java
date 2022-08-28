package org.quiltmc.qsl.component.mixin.blockentity;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.container.ComponentContainerImpl;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements ComponentProvider {
	private ComponentContainer qsl$container;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initComponents(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		this.qsl$container = new ComponentContainerImpl(this, ComponentsImpl.getInjections(this));
	}

	@Override
	public ComponentContainer getComponentContainer() {
		return this.qsl$container;
	}

	@Override
	public @Nullable SyncChannel<?, ?> getSyncChannel() {
		return SyncChannel.BLOCK_ENTITY;
	}

	@Inject(method = "writeNbt", at = @At("HEAD"))
	private void writeComponentNbt(NbtCompound nbt, CallbackInfo cir) {
		this.qsl$container.writeNbt(nbt);
	}

	@Inject(method = "readNbt", at = @At("HEAD"))
	private void readComponentNbt(NbtCompound nbt, CallbackInfo ci) {
		this.qsl$container.readNbt(nbt);
	}
}
