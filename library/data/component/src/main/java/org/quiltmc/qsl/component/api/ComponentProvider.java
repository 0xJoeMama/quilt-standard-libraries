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

package org.quiltmc.qsl.component.api;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.quiltmc.qsl.base.api.util.InjectedInterface;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.sync.SyncChannel;

@InjectedInterface({ // We inject this inteface, so that modders don't need to use the methods in Components directly with our default implementations.
	Entity.class,
	BlockEntity.class,
	Chunk.class,
	MinecraftServer.class, // MinecraftServer and MinecraftClient contain Level components
	MinecraftClient.class,
	World.class
})
public interface ComponentProvider {
	default ComponentContainer getComponentContainer() {
		throw new AbstractMethodError("You need to implement the getComponentContainer method on your provider!");
	}

	@Nullable
	default SyncChannel<?, ?> getSyncChannel() {
		return null;
	}

	@Nullable
	default <C> C expose(ComponentType<C> type) {
		return this.getComponentContainer().expose(type);
	}

	@Nullable
	default <C> C ifPresent(ComponentType<C> type, Consumer<? super C> action) {
		C component = this.expose(type);
		if (component != null) {
			action.accept(component);
		}

		return component;
	}
}
