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

package org.quiltmc.qsl.component.test;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.level.LevelProperties;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.components.FloatComponent;
import org.quiltmc.qsl.component.api.components.FunctionComponent;
import org.quiltmc.qsl.component.api.components.InventoryComponent;
import org.quiltmc.qsl.component.api.components.TickingComponent;
import org.quiltmc.qsl.component.api.event.ComponentEvents;
import org.quiltmc.qsl.component.impl.components.DefaultFloatComponent;
import org.quiltmc.qsl.component.impl.components.DefaultIntegerComponent;
import org.quiltmc.qsl.component.impl.components.DefaultInventoryComponent;

public class ComponentTestMod implements ModInitializer {
	public static final String MODID = "quilt_component_test";

	public static final ComponentType<InventoryComponent> COW_INVENTORY = Components.register(
			new Identifier(MODID, "cow_inventory"),
			() -> new DefaultInventoryComponent(() -> DefaultedList.ofSize(1, new ItemStack(Items.COBBLESTONE, 64)))
	);
	public static final ComponentType<DefaultIntegerComponent> CREEPER_EXPLODE_TIME = Components.register(
			new Identifier(MODID, "creeper_explode_time"),
			() -> new DefaultIntegerComponent(200)
	);
	public static final ComponentType<DefaultIntegerComponent> HOSTILE_EXPLODE_TIME = Components.register(
			new Identifier(MODID, "hostile_explode_time"),
			DefaultIntegerComponent::new
	);
	public static final ComponentType<DefaultIntegerComponent> CHEST_NUMBER = Components.register(
			new Identifier(MODID, "chest_number"),
			() -> new DefaultIntegerComponent(200)
	);
	public static final ComponentType<DefaultInventoryComponent> CHUNK_INVENTORY = Components.register(
			new Identifier(MODID, "chunk_inventory"),
			() -> new DefaultInventoryComponent(1)
	);
	public static final ComponentType<DefaultFloatComponent> SAVE_FLOAT = Components.register(
			new Identifier(MODID, "save_float"),
			DefaultFloatComponent::new
	);
	public static final ComponentType<TickingComponent> SERVER_TICK = Components.registerTicking(
			new Identifier(MODID, "level_tick"),
			provider -> {
				if (provider instanceof LevelProperties properties) {
					properties.expose(SAVE_FLOAT).ifPresent(floatComponent -> {
						floatComponent.set(floatComponent.get() + 0.5f);
						floatComponent.save();
					});
				}
			}
	);
	public static final Block TEST_BLOCK = new TestBlock(AbstractBlock.Settings.copy(Blocks.STONE));
	public static final ComponentType<DefaultIntegerComponent> ITEMSTACK_INT = Components.register(
			new Identifier(MODID, "itemstack_int"),
			DefaultIntegerComponent::new
	);
	public static final ComponentType<FunctionComponent<Unit, Unit>> FUNC_COMP = Components.registerStatic(
			new Identifier(MODID, "player_thing"),
			() -> (provider, unused) -> {
				if (provider instanceof PlayerEntity entity) {
					entity.giveItemStack(new ItemStack(Items.WHEAT));
				}
				return Unit.INSTANCE;
			}
	);
	public static final ComponentType<TickingComponent> PLAYER_TICK = Components.registerTicking(
			new Identifier(MODID, "player_tick"),
			provider -> {
				if (provider instanceof ServerPlayerEntity player) {
					ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);
					if (stackInHand.isOf(Items.WHEAT)) {
						player.call(ComponentTestMod.FUNC_COMP, Unit.INSTANCE)
								.ifPresent(unit -> player.sendMessage(Text.literal("Prankt"), true));
					}
					var props = player.getWorld().getServer().getSaveProperties();
					if (props instanceof LevelProperties levelProperties && player.getWorld().getTime() % 100 == 0) {
						player.sendMessage(Text.literal(
								levelProperties.expose(SAVE_FLOAT).map(FloatComponent::get).orElse(0f).toString()
						), false);
					}
				}
			}
	);
	public static final ComponentType<DefaultIntegerComponent> TEST_BE_INT = Components.register(
			new Identifier(ComponentTestMod.MODID, "test_be_int"),
			DefaultIntegerComponent::new
	);

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.BLOCK, new Identifier(MODID, "test_block"), TEST_BLOCK);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MODID, "block_entity"), TEST_BE_TYPE);
		Block.STATE_IDS.add(TEST_BLOCK.getDefaultState());

		// Cached Injection
		Components.inject(CreeperEntity.class, CREEPER_EXPLODE_TIME);
		Components.injectInheritage(CowEntity.class, COW_INVENTORY);
		Components.injectInheritanceExcept(HostileEntity.class, HOSTILE_EXPLODE_TIME, CreeperEntity.class);
		Components.inject(ChestBlockEntity.class, CHEST_NUMBER);
		Components.injectInheritage(Chunk.class, CHUNK_INVENTORY);
		Components.inject(LevelProperties.class, SAVE_FLOAT);
		Components.injectInheritage(ServerPlayerEntity.class, FUNC_COMP);
		Components.inject(LevelProperties.class, SERVER_TICK);

		// Dynamic Injection
		ComponentEvents.DYNAMIC_INJECT.register((provider, injector) -> {
			injector.injectIf(provider instanceof ItemStack stack && stack.isOf(Items.BOOKSHELF), ITEMSTACK_INT);
			injector.injectIf(provider instanceof PlayerEntity, PLAYER_TICK);
		});
	}

	public static final BlockEntityType<TestBlockEntity> TEST_BE_TYPE =
			BlockEntityType.Builder.create(TestBlockEntity::new, TEST_BLOCK, Blocks.NOTE_BLOCK).build(null);

}
