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

package org.quiltmc.qsl.component.api.sync;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.component.Syncable;
import org.quiltmc.qsl.component.api.sync.codec.NetworkCodec;
import org.quiltmc.qsl.component.impl.client.sync.ClientResolution;
import org.quiltmc.qsl.component.impl.sync.packet.PacketIds;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PlayerLookup;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

// ClientResolution.* all reference client-only classes,
// so we need to just make sure we use lambdas so no there is no attempt to classload non-existent classes
@SuppressWarnings("Convert2MethodRef")
public class SyncChannel<P extends ComponentProvider, U> {
	// BlockEntity
	public static final SyncChannel<BlockEntity, BlockPos> BLOCK_ENTITY = new SyncChannel<>(
			PacketIds.BLOCK_ENTITY_SYNC,
			NetworkCodec.BLOCK_POS,
			BlockEntity::getPos,
			blockPos -> ClientResolution.blockEntity(blockPos),
			PlayerLookup::tracking
	);

	// Entity
	public static final SyncChannel<Entity, Integer> ENTITY = new SyncChannel<>(
			PacketIds.ENTITY_SYNC,
			NetworkCodec.VAR_INT,
			Entity::getId,
			id -> ClientResolution.entity(id),
			PlayerLookup::tracking
	);

//	// Chunk
//	public static final SyncChannel<Chunk, ChunkPos> CHUNK = new SyncChannel<>(
//			PacketIds.CHUNK_SYNC,
//			NetworkCodec.CHUNK_POS,
//			Chunk::getPos,
//			chunkPos -> ClientResolution.chunk(chunkPos),
//			chunk -> PlayerLookup.tracking(((ServerWorld) ((WorldChunk) chunk).getWorld()), chunk.getPos())
//			// only called server side so the cast is safe
//	); // **Careful**: This only works with WorldChunks not other chunk types!
//
//	// World
//	public static final SyncChannel<World, Unit> WORLD = new SyncChannel<>(
//			PacketIds.WORLD_SYNC,
//			NetworkCodec.EMPTY,
//			world -> Unit.INSTANCE,
//			unit -> ClientResolution.world(),
//			world -> PlayerLookup.world((ServerWorld) world)// only called server side so the cast is safe
//	);
//
//	// Level
//	public static final SyncChannel<ComponentProvider, Unit> LEVEL = new SyncChannel<>(
//			PacketIds.LEVEL_SYNC,
//			NetworkCodec.EMPTY,
//			provider -> Unit.INSTANCE,
//			unit -> ClientResolution.level(),
//			provider -> PlayerLookup.all((MinecraftServer) provider) // only called server side so the cast is safe
//	);

	protected final Identifier channelId;
	protected final NetworkCodec<U> codec;
	protected final Function<P, U> identifyingDataTransformer;
	protected final Function<U, P> clientLocator;
	protected final Function<? super P, Collection<ServerPlayerEntity>> playerProvider;

	public SyncChannel(Identifier channelId, NetworkCodec<U> codec,
			Function<P, U> identifyingDataTransformer,
			Function<U, P> clientLocator,
			Function<? super P, Collection<ServerPlayerEntity>> playerProvider) {
		this.channelId = channelId;
		this.codec = codec;
		this.identifyingDataTransformer = identifyingDataTransformer;
		this.clientLocator = clientLocator;
		this.playerProvider = playerProvider;
	}

	public static void createPacketChannels(Consumer<SyncChannel<?, ?>> register) {
		register.accept(BLOCK_ENTITY);
		register.accept(ENTITY);
//		register.accept(CHUNK);
//		register.accept(WORLD);
//		register.accept(LEVEL);
	}

	@Environment(EnvType.CLIENT)
	public P toClientProvider(PacketByteBuf buf) {
		return this.clientLocator.apply(this.codec.decode(buf));
	}

	public PacketByteBuf toClientBuffer(P p) {
		var buf = PacketByteBufs.create();
		this.codec.encode(buf, this.identifyingDataTransformer.apply(p));
		return buf;
	}

	public void sync(Collection<ComponentType<?>> pendingSync, ComponentProvider provider) {
		this.sync(pendingSync, provider, List.of());
	}

	public void sync(Collection<ComponentType<?>> pendingSync, ComponentProvider provider,
			Collection<ServerPlayerEntity> players) {
		if (pendingSync.isEmpty()) {
			return;
		}

		this.send(players, provider, buf -> { // calling 'send' appends the provider data
			buf.writeInt(pendingSync.size()); // append size

			for (var type : pendingSync) {
				var component = ((Syncable) provider.expose(type));
				if (component == null) continue;

				ComponentType.NETWORK_CODEC.encode(buf, type); // append component type rawId
				component.writeToBuf(buf);                       // append component data
			}
		});
	}

	@Environment(EnvType.CLIENT)
	public void handleServerPushedSync(MinecraftClient client, PacketByteBuf buf) {
		buf.retain(); // hold the buffer in memory

		client.execute(() -> {
			// provider data is consumed by SyncChannel#toClientProvider
			ComponentProvider provider = this.toClientProvider(buf);

			int size = buf.readInt(); // consume size

			if (provider == null) {
				return;
			}

			for (int i = 0; i < size; i++) {
				ComponentType<?> type = ComponentType.NETWORK_CODEC.decode(buf); // consume rawId
				provider.ifPresent(type, o -> ((Syncable) o).readFromBuf(buf));
			}

			buf.release(); // make sure the buffer is properly freed
		});
	}

	public Identifier getChannelId() {
		return this.channelId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof SyncChannel<?, ?> that)) return false;
		return this.channelId.equals(that.channelId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.channelId);
	}

	@Override
	public String toString() {
		return "SyncChannel[%s]".formatted(this.channelId);
	}

	@SuppressWarnings("unchecked")
	protected void send(Collection<ServerPlayerEntity> players, ComponentProvider provider, BufferFiller filler) {
		// The casting to P should never fail, since the provider and
		// the channel type *must* match for the implementation to be correct anyway
		P providerAsP = (P) provider;
		var buf = this.toClientBuffer(providerAsP); // append provider data
		filler.fill(buf);                                         // append all the container data
		ServerPlayNetworking.send(
				players.isEmpty() ? this.playerProvider.apply(providerAsP) : players, this.channelId, buf
		);
	}

	@FunctionalInterface
	public interface BufferFiller {
		void fill(PacketByteBuf buf);
	}
}
