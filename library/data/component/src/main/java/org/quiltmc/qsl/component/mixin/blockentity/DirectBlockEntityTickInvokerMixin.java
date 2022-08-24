package org.quiltmc.qsl.component.mixin.blockentity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.BlockEntity;

@Mixin(targets = "net/minecraft/world/chunk/WorldChunk$DirectBlockEntityTickInvoker")
public class DirectBlockEntityTickInvokerMixin {
	@Shadow
	@Final
	private BlockEntity blockEntity;

	@Inject(
			method = "tick", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/World;" +
					 "Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;" +
					 "Lnet/minecraft/block/entity/BlockEntity;)V"
		)
	)
	private void tickComponents(CallbackInfo ci) {
		this.blockEntity.getComponentContainer().tick();
	}
}
