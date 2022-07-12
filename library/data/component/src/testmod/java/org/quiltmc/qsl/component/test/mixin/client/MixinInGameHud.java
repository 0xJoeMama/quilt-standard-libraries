/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.component.test.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import org.quiltmc.qsl.component.api.components.IntegerComponent;
import org.quiltmc.qsl.component.test.ComponentTestMod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

	@Shadow
	@Final
	private ItemRenderer itemRenderer;

	@Shadow
	public abstract TextRenderer getTextRenderer();

	@Inject(method = "render", at = @At("TAIL"))
	private void renderCustom(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		Entity entity = MinecraftClient.getInstance().targetedEntity;
		if (entity != null) {
			entity.expose(ComponentTestMod.HOSTILE_EXPLODE_TIME).map(IntegerComponent::get).ifJust(integer -> {
				this.getTextRenderer().draw(matrices, integer.toString(), 10, 10, 0xfafafa);
			});
		}
		ChunkPos chunkPos = MinecraftClient.getInstance().player.getChunkPos();
		MinecraftClient.getInstance().world.getChunk(chunkPos.x, chunkPos.z).expose(ComponentTestMod.CHUNK_INVENTORY)
				.map(defaultInventoryComponent -> defaultInventoryComponent.getStack(0))
				.ifJust(itemStack -> this.itemRenderer.renderInGui(itemStack, 10, 10));
	}
}
