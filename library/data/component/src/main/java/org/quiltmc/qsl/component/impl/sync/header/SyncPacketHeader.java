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

package org.quiltmc.qsl.component.impl.sync.header;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.impl.CommonInitializer;
import org.quiltmc.qsl.component.impl.sync.ComponentHeaderRegistry;
import org.quiltmc.qsl.component.impl.sync.codec.NetworkCodec;
import org.quiltmc.qsl.networking.api.PacketByteBufs;

public record SyncPacketHeader<P extends ComponentProvider>(@NotNull NetworkCodec<P> codec) {
	public static final SyncPacketHeader<BlockEntity> BLOCK_ENTITY = new SyncPacketHeader<>(NetworkCodec.BLOCK_ENTITY);
	public static final SyncPacketHeader<Entity> ENTITY = new SyncPacketHeader<>(NetworkCodec.ENTITY);
	public static final SyncPacketHeader<Chunk> CHUNK = new SyncPacketHeader<>(NetworkCodec.CHUNK);
	// TODO: LevelProperties sync
//	public static final SyncPacketHeader<?> LEVEL = new SyncPacketHeader<>(NetworkCodec.LEVEL);

	public static void registerDefaults() {
		ComponentHeaderRegistry.register(CommonInitializer.id("block_entity"), BLOCK_ENTITY);
		ComponentHeaderRegistry.register(CommonInitializer.id("entity"), ENTITY);
		ComponentHeaderRegistry.register(CommonInitializer.id("chunk"), CHUNK);
	}

	public @NotNull PacketByteBuf start(@NotNull ComponentProvider provider) {
		var buf = PacketByteBufs.create();
		buf.writeInt(ComponentHeaderRegistry.HEADERS.getRawId(this));
		//noinspection unchecked the person calling is responsible to make sure we get a valid provider instance!
		this.codec.encode(buf, (P) provider);

		return buf;
	}
}
